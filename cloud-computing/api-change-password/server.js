require('dotenv').config();
const Hapi = require('@hapi/hapi');
const Boom = require('@hapi/boom');
const Joi = require('joi');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const mongoose = require('mongoose'); // Impor mongoose

// Model untuk user (harus didefinisikan)
const userSchema = new mongoose.Schema({
    email: { type: String, required: true, unique: true },
    password: { type: String, required: true },
});

const User = mongoose.model('Users', userSchema);

// Koneksi ke MongoDB
mongoose.connect(process.env.MONGODB_URI)
  .then(() => console.log('MongoDB Connected'))
  .catch(err => {
    console.error('MongoDB Connection Error:', err);
    process.exit(1);
  });

// Middleware autentikasi
const validateToken = async (request, h) => {
    const token = request.headers.authorization;

    if (!token || !token.startsWith('Bearer ')) {
        throw Boom.unauthorized('Token not provided or incorrect format');
    }

    try {
        const decoded = jwt.verify(token.split(' ')[1], process.env.JWT_SECRET); // Hapus 'Bearer'
        request.auth = { userId: decoded.userId };  // Decode and assign userId from token payload
        return h.continue;
    } catch (err) {
        console.error('Token validation error:', err.message);
        throw Boom.unauthorized('Invalid token');
    }
};

// API untuk ganti password
const changePasswordHandler = async (request, h) => {
    const { recentPassword, newPassword } = request.payload;
    const userId = request.auth.userId;

    try {
        // Pastikan userId adalah string yang valid sebagai ObjectId
        if (!mongoose.Types.ObjectId.isValid(userId)) {
            return h.response({ status: 'error', message: 'Invalid userId' }).code(400);
        }

        const user = await User.findById(userId); // Gunakan mongoose model untuk menemukan user

        if (!user) {
            return h.response({ status: 'error', message: 'User not found' }).code(404);
        }

        // Verifikasi password saat ini
        const isMatch = await bcrypt.compare(recentPassword, user.password);
        if (!isMatch) {
            return h.response({ status: 'error', message: 'Incorrect recent password' }).code(400);
        }

        // Cek jika password baru sama dengan password lama
        const isSameAsOld = await bcrypt.compare(newPassword, user.password);
        if (isSameAsOld) {
            return h.response({ status: 'error', message: 'New password cannot be the same as the old password' }).code(400);
        }

        // Hash password baru
        const hashedPassword = await bcrypt.hash(newPassword, 10);

        // Update password di database
        user.password = hashedPassword;
        await user.save();

        return h.response({ message: 'Password changed successfully' }).code(200);
    } catch (error) {
        console.error('Error changing password:', error);
        return h.response({ message: 'Internal Server Error' }).code(500);
    }
};

// Inisialisasi server
const server = Hapi.server({
    port: process.env.PORT || 3000,
    host: 'localhost',
});

// Rute untuk ganti password
server.route({
    method: 'PUT',
    path: '/change-password',
    options: {
        pre: [validateToken],  // Menggunakan middleware untuk autentikasi
        validate: {
            payload: Joi.object({
                recentPassword: Joi.string().required(),
                newPassword: Joi.string().min(8).required(),
                confirmNewPassword: Joi.string().valid(Joi.ref('newPassword')).required(),
            }),
        },
    },
    handler: changePasswordHandler,
});

// Start server
const startServer = async () => {
    try {
        await server.start();
        console.log('Server running on %s', server.info.uri);
    } catch (err) {
        console.error('Error starting server:', err);
        process.exit(1);
    }
};

// Graceful Shutdown
process.on('SIGINT', async () => {
    console.log('Shutting down server...');
    await server.stop();
    console.log('Server stopped');
    process.exit(0);
});

startServer();
