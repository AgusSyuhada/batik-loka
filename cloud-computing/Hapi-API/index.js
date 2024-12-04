const path = require("path");
require("dotenv").config({
  path: path.join(__dirname, ".env"),
});
const Hapi = require("@hapi/hapi");
const nodemailer = require("nodemailer");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const User = require("./models/user");
const db = require("./fstore-config");
const multer = require("multer");

const storage = multer.memoryStorage(); // File akan disimpan di memory sementara
const upload = multer({ storage });

// Create Hapi Server
const server = Hapi.server({
  port: 5000,
  host: "localhost",
});

// Email transporter setup for OTP
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS,
  },
});

// Send OTP function
const sendOtpEmail = async (email, otp) => {
  const mailOptions = {
    from: process.env.EMAIL_USER,
    to: email,
    subject: "Your OTP Code",
    text: `Your OTP code is: ${otp}`,
  };

  await transporter.sendMail(mailOptions);
};

// Registration Route
server.route({
  method: "POST",
  path: "/register",
  handler: async (request, h) => {
    try {
      const { username, email, password, confirmPassword } = request.payload;

      // Check if all fields are provided
      if (!username || !email || !password || !confirmPassword) {
        return h.response({ error: "All fields are required" }).code(400);
      }

      // Check if password and confirmPassword match
      if (password !== confirmPassword) {
        return h.response({ error: "Passwords do not match" }).code(400);
      }

      // Ambil pengguna dari Firestore
      const userDoc = await db.collection("users").doc(email).get();

      if (userDoc.exists) {
        return res.status(500).json({ message: "User exist." });
      }

      // Hash password
      const hashedPassword = await bcrypt.hash(password, 10);

      const otp = Math.floor(1000 + Math.random() * 9000).toString();

      // Simpan pengguna ke Firestore
      await db.collection("users").doc(email).set({
        name,
        email,
        password: hashedPassword,
        isVerified: false,
        otp,
        avatarUrl:
          "https://storage.googleapis.com/bucket-project21/edit-profile/avatar.png",
        streak: 0,
        lastHit: new Date().toISOString(),
      });

      // Hash the password
      const hashedPassword = await bcrypt.hash(password, 10);

      // Generate OTP
      const otp = Math.floor(100000 + Math.random() * 900000).toString();

      // Create new user
      const newUser = new User({
        username,
        email,
        password: hashedPassword,
        otp,
        otpExpires: Date.now() + 10 * 60000,
        isVerified: false, // Not verified until OTP is validated
      });

      // Save user to the database
      await newUser.save();

      // Send OTP to user's email
      await sendOtpEmail(email, otp);

      // Respond with success message
      return h
        .response({
          message:
            "Registration successful. Please verify your account using the OTP sent to your email.",
        })
        .code(200);
    } catch (err) {
      console.error("Error in /register route:", err);
      return h.response({ error: "Internal Server Error" }).code(500);
    }
  },
});

// OTP Verification Route
server.route({
  method: "POST",
  path: "/verify-otp",
  handler: async (request, h) => {
    try {
      const { otp } = request.payload;

      if (!otp) {
        return h.response({ error: "OTP is required" }).code(400);
      }

      // Find user by OTP
      const user = await User.findOne({ otp });
      if (!user) {
        return h.response({ error: "Invalid OTP" }).code(400);
      }

      // Check if OTP is expired
      if (Date.now() > user.otpExpires) {
        return h.response({ error: "OTP has expired" }).code(400);
      }

      // Update user as verified
      user.isVerified = true;
      user.otp = null; // Clear OTP
      user.otpExpires = null; // Clear OTP expiration time
      await user.save();

      return h
        .response({ message: "Account verified successfully." })
        .code(200);
    } catch (err) {
      console.error("Error in /verify-otp route:", err);
      return h.response({ error: "Internal Server Error" }).code(500);
    }
  },
});

// Login Route
server.route({
  method: "POST",
  path: "/login",
  handler: async (request, h) => {
    try {
      const { email, password } = request.payload;

      if (!email || !password) {
        return h
          .response({ error: "Email and password are required" })
          .code(400);
      }

      // Check if user exists
      const user = await User.findOne({ email });
      if (!user) {
        return h.response({ error: "User not found" }).code(404);
      }

      // Check if user is verified
      if (!user.isVerified) {
        return h
          .response({
            error: "Account not verified. Please verify your account first.",
          })
          .code(400);
      }

      // Validate password
      const isMatch = await bcrypt.compare(password, user.password);
      if (!isMatch) {
        return h.response({ error: "Invalid password" }).code(400);
      }

      // Generate JWT token
      const token = jwt.sign({ userId: user._id }, process.env.JWT_SECRET, {
        expiresIn: "12h",
      });

      return h.response({ message: "Login successful", token }).code(200);
    } catch (err) {
      console.error("Error in /login route:", err);
      return h.response({ error: "Internal Server Error" }).code(500);
    }
  },
});

// Start server
const start = async () => {
  try {
    await server.start();
    console.log("Server running on %s", server.info.uri);
  } catch (err) {
    console.log(err);
    process.exit(1);
  }
};

start();
