const express = require("express");
const path = require("path");
const multer = require("multer");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const nodemailer = require("nodemailer");
const { Storage } = require("@google-cloud/storage");
const db = require("./fstore-config");
require("dotenv").config({ path: path.join(__dirname, ".env") });

// Setup Express app
const app = express();
const port = 5000;

// Middleware to parse JSON payloads
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Setup Multer to handle file uploads in memory
const storage = multer.memoryStorage();
const upload = multer({ storage: storage });

// Google Cloud Storage setup
const gcs = new Storage({
  keyFilename: path.join(__dirname, "gcp.json"),
});
const bucketName = "batikloka_cloudbuild";
const bucket = gcs.bucket(bucketName);

// Email transporter setup for OTP
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS,
  },
});

const JWT_SECRET = process.env.JWT_SECRET;

// Send OTP function
const sendOTP = async (email, otp) => {
  const mailOptions = {
    from: process.env.EMAIL_USER,
    to: email,
    subject: "Your OTP Code",
    text: `Your OTP code is: ${otp}`,
  };

  transporter.sendMail(mailOptions, (error, info) => {
    if (error) {
      console.log("Error sending OTP:", error);
    } else {
      console.log("OTP sent: " + info.response);
    }
  });
};

// Middleware Authentication
const validateToken = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith("Bearer ")) {
      throw new Error("Token missing or invalid");
    }

    const token = authHeader.split(" ")[1];
    const decoded = jwt.verify(token, JWT_SECRET);
    req.auth = { email: decoded.email };
    next();
  } catch (err) {
    console.error("Token validation error:", err.message);
    res.status(401).json({ message: "Unauthorized" });
  }
};

// Registration Route
app.post("/register", async (req, res) => {
  try {
    const { name, email, password, confirmPassword } = req.body;

    // Check if all fields are provided
    if (!name || !email || !password || !confirmPassword) {
      return res.status(400).json({ error: "All fields are required" });
    }

    // Check if password and confirmPassword match
    if (password !== confirmPassword) {
      return res.status(400).json({ error: "Passwords do not match" });
    }

    // Check if user exists in Firestore
    const userDoc = await db.collection("users").doc(email).get();

    if (userDoc.exists) {
      return res.status(403).json({ error: "User exists" });
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(password, 10);

    const otp = Math.floor(100000 + Math.random() * 900000).toString();

    // Save user to Firestore
    await db
      .collection("users")
      .doc(email)
      .set({
        name,
        email,
        password: hashedPassword,
        isVerified: false,
        otp,
        otpExpires: Date.now() + 5 * 60000,
        avatarUrl: null,
      });

    // Send OTP to user's email
    sendOTP(email, otp);

    // Respond with success message
    return res.status(201).json({
      message:
        "Registration successful. Please verify your account using the OTP sent to your email.",
    });
  } catch (err) {
    console.error("Error in /register route:", err);
    return res.status(500).json({ error: "Internal Server Error" });
  }
});

// OTP Verification Route
app.post("/verify-otp", async (req, res) => {
  try {
    const { email, otp } = req.body;

    if (!otp) {
      return res.status(400).json({ error: "OTP is required" });
    }

    const userDoc = await db.collection("users").doc(email).get();

    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }

    const user = userDoc.data();

    if (user.isVerified) {
      return res.status(500).json({ message: "User has been verified." });
    }

    if (user.otp !== otp) {
      return res.status(403).json({ message: "Invalid OTP." });
    }

    await db.collection("users").doc(email).update({
      isVerified: true,
      otp: null,
    });

    return res.status(200).json({ message: "Account verified successfully." });
  } catch (err) {
    console.error("Error in /verify-otp route:", err);
    return res.status(500).json({ error: "Internal Server Error" });
  }
});

// Login Route
app.post("/login", async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ error: "Email and password are required" });
    }

    const userDoc = await db.collection("users").doc(email).get();

    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }

    const user = userDoc.data();

    if (!user.isVerified) {
      return res.status(500).json({ message: "User is not verified yet." });
    }

    // Check password
    const isPasswordValid = await bcrypt.compare(password, user.password);
    if (!isPasswordValid) {
      return res.status(401).json({ message: "Invalid password." });
    }

    // Generate JWT token
    const token = jwt.sign({ email: user.email }, JWT_SECRET, {
      expiresIn: "12h",
    });

    return res.status(200).json({ message: "Login successful", token });
  } catch (err) {
    console.error("Error in /login route:", err);
    return res.status(500).json({ error: "Internal Server Error" });
  }
});

// Route untuk ganti username
app.put("/change-name", validateToken, async (req, res) => {
  try {
    const { newName } = req.body;
    const { email: userEmail } = req.auth;

    const userDoc = await db.collection("users").doc(userEmail).get();

    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }

    const updates = { name: newName };

    await db.collection("users").doc(userEmail).update(updates);

    return res.status(200).json({ message: "Username updated successfully" });
  } catch (err) {
    console.error("Error updating username:", err);
    return res.status(500).json({ message: "Internal server error" });
  }
});

// Avatar Update Route
app.post(
  "/change-avatar",
  validateToken,
  upload.single("avatar"),
  async (req, res) => {
    try {
      if (!req.file) {
        return res.status(400).json({ error: "No file uploaded" });
      }

      const { email: userEmail } = req.auth;
      const userDoc = await db.collection("users").doc(userEmail).get();

      if (!userDoc.exists) {
        return res.status(404).json({ message: "User not found." });
      }

      const folderName = "avatar";
      const fileName = `${folderName}/${Date.now()}-${req.file.originalname.replace(
        /\s+/g,
        "_"
      )}`;
      const blob = bucket.file(fileName);

      const blobStream = blob.createWriteStream({
        resumable: false,
        contentType: req.file.mimetype,
      });

      blobStream.on("error", (err) => {
        console.error("Error uploading file:", err);
        return res.status(500).json({ message: "File upload failed" });
      });

      blobStream.on("finish", async () => {
        const avatarUrl = `https://storage.googleapis.com/${bucketName}/${fileName}`;
        await db.collection("users").doc(userEmail).update({
          avatarUrl,
        });

        return res
          .status(200)
          .json({ message: "Avatar updated successfully", avatarUrl });
      });

      blobStream.end(req.file.buffer);
    } catch (err) {
      console.error("Error updating avatar:", err);
      return res.status(500).json({ message: "Internal server error" });
    }
  }
);

// Endpoint untuk memulai proses Forget Password
app.post("/forget-password", async (req, res) => {
  const { email } = req.body;

  if (!email) {
    return res.status(400).json({ message: "Email is required." });
  }

  try {
    // Ambil pengguna dari Firestore
    const userDoc = await db.collection("users").doc(email).get();

    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }

    const otp = Math.floor(100000 + Math.random() * 900000).toString();

    // Update OTP di database
    await db.collection("users").doc(email).update({
      otp,
    });

    sendOTP(email, otp); // Kirim OTP ke email

    res.status(200).json({ message: "OTP sent to your email." });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// Endpoint untuk mengubah password
app.post("/reset-password", async (req, res) => {
  const { email, otp, newPassword } = req.body;

  if (!email || !otp || !newPassword) {
    return res
      .status(400)
      .json({ message: "Email, OTP, and new password are required." });
  }

  try {
    // Ambil pengguna dari Firestore
    const userDoc = await db.collection("users").doc(email).get();

    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }

    const user = userDoc.data();

    if (user.otp != otp) {
      return res.status(403).json({ message: "Invalid OTP." });
    }

    // Hash password baru
    const hashedPassword = await bcrypt.hash(newPassword, 10);

    // Update password di database
    await db.collection("users").doc(email).update({
      password: hashedPassword,
      otp: null, // Hapus OTP setelah digunakan
    });

    res.status(200).json({ message: "Password successfully reset." });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

app.get("/profile", validateToken, async (req, res) => {
  try {
    // Ambil pengguna berdasarkan email dari JWT
    const userEmail = req.auth.email;
    const userDoc = await db.collection("users").doc(userEmail).get();

    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }

    const user = userDoc.data();

    // Return profil pengguna
    res.status(200).json({
      name: user.name,
      email: user.email,
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// Start Express server
app.listen(port, () => {
  console.log(`Server running on http://localhost:${port}`);
});
