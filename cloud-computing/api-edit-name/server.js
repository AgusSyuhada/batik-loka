require('dotenv').config(); // Pastikan ini ada di awal
const Hapi = require('@hapi/hapi');
const Joi = require('joi');
const jwt = require('jsonwebtoken');
const mongoose = require('mongoose');

// Ambil konfigurasi dari .env
const JWT_SECRET = process.env.JWT_SECRET;

// Koneksi ke MongoDB menggunakan Mongoose
mongoose
  .connect(process.env.MONGODB_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
  })
  .then(() => console.log('MongoDB connected'))
  .catch((err) => {
    console.error('MongoDB connection error:', err);
    process.exit(1); // Keluar jika koneksi gagal
  });

// Schema dan model untuk koleksi users
const userSchema = new mongoose.Schema({
  username: { type: String, required: true },
});

const User = mongoose.model('User', userSchema);

// Middleware Authentication
const validateToken = async (request, h) => {
  try {
    const authHeader = request.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      throw new Error('Token missing or invalid');
    }

    const token = authHeader.split(' ')[1];
    const decoded = jwt.verify(token, JWT_SECRET); // Verifikasi token menggunakan JWT_SECRET
    request.auth = { userId: decoded.userId };
    return h.continue;
  } catch (err) {
    console.error('Token validation error:', err.message);
    return h.response({ message: 'Unauthorized' }).code(401).takeover();
  }
};

// Inisialisasi server
const init = async () => {
  const server = Hapi.server({
    port: 4000,
    host: 'localhost',
  });

  // Route untuk mengganti username
  server.route({
    method: 'PUT',
    path: '/change-username',
    options: {
      pre: [{ method: validateToken }], // Middleware untuk autentikasi
      validate: {
        payload: Joi.object({
          newUsername: Joi.string().min(3).max(20).required(),
        }),
      },
    },
    handler: async (request, h) => {
      const { newUsername } = request.payload;
      const { userId } = request.auth;

      try {
        // Cari user berdasarkan ID dan update username
        const user = await User.findByIdAndUpdate(
          userId,
          { username: newUsername },
          { new: true } // Mengembalikan dokumen yang diperbarui
        );

        if (!user) {
          return h.response({ message: 'User not found' }).code(404);
        }

        return h.response({ message: 'Username updated successfully' });
      } catch (err) {
        console.error('Error updating username:', err);
        return h.response({ message: 'Internal server error' }).code(500);
      }
    },
  });

  // Start server
  await server.start();
  console.log(`Server running on ${server.info.uri}`);
};

// Menangani error tidak terduga
process.on('unhandledRejection', (err) => {
  console.error('Unhandled Rejection:', err);
  process.exit(1);
});

// Tutup koneksi MongoDB saat server berhenti
process.on('SIGINT', async () => {
  await mongoose.disconnect();
  console.log('MongoDB connection closed');
  process.exit(0);
});

init();
