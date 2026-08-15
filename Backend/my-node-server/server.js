const express = require('express');
const dotenv = require('dotenv');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const connectDB = require('./db');
const User = require('./models/user');
const Order = require('./models/order');
const mongoose = require('mongoose');
const authMiddleware = require('./authMiddleware');
const { OAuth2Client } = require('google-auth-library');
const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);
const nodemailer = require('nodemailer');
const crypto = require('crypto'); 
const Announcement = require('./models/announcement'); 
const Transaction = require('./models/transaction'); 
const { isDriverOrOwner, isOwner } = require('./roleMiddleware');
const cron = require('node-cron'); 
const Subscription = require('./models/subscription');
 

dotenv.config();
const app = express();
app.use(express.json());

connectDB();


//activing corn job 224/7
app.get('/', (req, res) => {
    res.status(200).send('AquaServe API is awake and running!');
});



// const transporter = nodemailer.createTransport({
//     service: 'gmail',
//     auth: {
//         user: process.env.EMAIL_USER,
//         pass: process.env.EMAIL_PASS 
//     }
// });
const transporter = nodemailer.createTransport({
    host: 'smtp.gmail.com',
    port: 465,
    secure: true,
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    },
    family: 4 
});

//login
app.post('/api/login', async (req, res) => {
    const { email, password } = req.body;

    //Owner Login
    if (email === process.env.OWNER_EMAIL && password === process.env.OWNER_PASSWORD) {
        const payload = { user: { id: 'owner', role: 'owner' } };
        const token = jwt.sign(payload, process.env.JWT_SECRET, { expiresIn: '1d' });
        return res.status(200).json({ message: 'Owner login successful', role: 'owner', token });
    }

    //Driver login
    const drivers = [
        { email: 'driver1@aquaserve.com', pass: 'driver1pass', area: 'Jay Nagar' },
        { email: 'driver2@aquaserve.com', pass: 'driver2pass', area: 'Jadavji Nagar' },
        { email: 'driver3@aquaserve.com', pass: 'driver3pass', area: 'PramukhSwami Nagar' }
    ];

    const matchedDriver = drivers.find(d => d.email === email && d.pass === password);

    if (matchedDriver) {
        const payload = { 
            user: { 
                id: 'driver', 
                role: 'driver', 
                area: matchedDriver.area 
            } 
        };
        const token = jwt.sign(payload, process.env.JWT_SECRET, { expiresIn: '1d' });
        return res.status(200).json({ 
            message: `Login successful for ${matchedDriver.area}`, 
            role: 'driver', 
            token 
        });
    }

    //User Login
    if (!email || !password) {
        return res.status(400).json({ message: 'Please enter all fields' });
    }
    try {
        const user = await User.findOne({ email });
        if (!user) {
            return res.status(400).json({ message: 'Invalid credentials' });
        }
        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(400).json({ message: 'Invalid credentials' });
        }
        const payload = { user: { id: user.id, role: 'user' } };
        const token = jwt.sign(payload, process.env.JWT_SECRET, { expiresIn: '1d' });
        
        res.status(200).json({
            message: 'Login successful',
            role: 'user', 
            user: { id: user.id, name: user.name, email: user.email },
            token: token
        });
    } catch (err) {
        console.error('LOGIN SERVER ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//Signup
app.post('/api/signup', async (req, res) => {
    const { name, email, password } = req.body;
    if (!name || !email || !password) {
        return res.status(400).json({ message: 'Please enter all fields' });
    }
    try {
        let user = await User.findOne({ email });
        if (user) {
            return res.status(400).json({ message: 'User with that email already exists' });
        }
        user = new User({ name, email, password });
        const salt = await bcrypt.genSalt(10);
        user.password = await bcrypt.hash(password, salt);
        await user.save();
        const payload = { user: { id: user.id, role: 'user' } };
        const token = jwt.sign(payload, process.env.JWT_SECRET, { expiresIn: '1d' });
        res.status(201).json({
            message: 'Signup successful',
            user: { id: user.id, name: user.name, email: user.email },
            token: token
        });
    } catch (err) {
        console.error('SIGNUP SERVER ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//google login
app.post('/api/auth/google', async (req, res) => {
    const { idToken } = req.body;
    try {
        const ticket = await client.verifyIdToken({
            idToken,
            audience: process.env.GOOGLE_CLIENT_ID,
        });
        const { name, email } = ticket.getPayload();
        
        let user = await User.findOne({ email });

        if (!user) {
            const dummyPassword = crypto.randomBytes(16).toString('hex');
            const salt = await bcrypt.genSalt(10);
            const hashedPassword = await bcrypt.hash(dummyPassword, salt);

            user = new User({ 
                name, 
                email,
                password: hashedPassword 
            });
            await user.save();
            console.log(`New Google user registered: ${email}`);
        }

        const payload = { user: { id: user.id, role: 'user' } };
        const token = jwt.sign(payload, process.env.JWT_SECRET, { expiresIn: '1d' });

        res.status(200).json({
            message: 'Google login successful',
            role: 'user',
            user: { id: user.id, name: user.name, email: user.email },
            token: token
        });
    } catch (err) {
        console.error('GOOGLE AUTH ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error during Google authentication' });
    }
});


//forgot password(send otp)
app.post('/api/forgot-password', async (req, res) => {
    const { email } = req.body;
    if (!email) return res.status(400).json({ message: 'Please provide an email.' });

    try {
        const user = await User.findOne({ email });
        if (!user) return res.status(200).json({ message: 'If your email is in our system, you will receive a code.' });

        const token = crypto.randomInt(100000, 999999).toString();
        user.resetPasswordToken = token;
        user.resetPasswordExpires = Date.now() + 2 * 60 * 1000; 
        await user.save();

        const mailOptions = {
            from: process.env.EMAIL_USER,
            to: user.email,
            subject: 'Your AquaServe Password Reset Code',
            text: `Your password reset code is: ${token}\n\nThis code will expire in 2 minutes.`
        };

        await transporter.sendMail(mailOptions);
        res.status(200).json({ message: 'If your email is in our system, you will receive a code.' });
    } catch (err) {
        console.error('FORGOT PASSWORD ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//verify otp and reset password
app.post('/api/reset-password', async (req, res) => {
    const { email, token, password } = req.body;
    if (!email || !token || !password) return res.status(400).json({ message: 'Please provide email, token, and new password.' });

    try {
        const user = await User.findOne({ 
            email,
            resetPasswordToken: token,
            resetPasswordExpires: { $gt: Date.now() } 
        });

        if (!user) return res.status(400).json({ message: 'Password reset code is invalid or has expired.' });

        const salt = await bcrypt.genSalt(10);
        user.password = await bcrypt.hash(password, salt);
        user.resetPasswordToken = undefined;
        user.resetPasswordExpires = undefined;
        await user.save();

        res.status(200).json({ message: 'Password has been reset successfully.' });
    } catch (err) {
        console.error('RESET PASSWORD ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//user information
app.get('/api/profile', authMiddleware, async (req, res) => {
    try {
        const user = await User.findById(req.user.id).select('-password');
        if (!user) return res.status(404).json({ message: 'User not found' });
        
        res.status(200).json({
            name: user.name,
            email: user.email,
            phoneNumber: user.phoneNumber,
            address: user.address,
            area: user.area, 
            postalCode: user.postalCode,
            extra: user.extra
        });
    } catch (err) {
        console.error('GET PROFILE ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//edit profile
app.put('/api/profile', authMiddleware, async (req, res) => {
    const { phone_number, address, area, postalCode, extra } = req.body; 
    try {
        const user = await User.findById(req.user.id);
        if (!user) return res.status(404).json({ message: 'User not found' });
        
        user.phoneNumber = phone_number || user.phoneNumber;
        user.address = address || user.address;
        user.area = area || user.area; 
        user.postalCode = postalCode || user.postalCode;
        user.extra = extra || user.extra;
        await user.save();
        
        res.status(200).json({ 
            message: 'Profile updated successfully',
            user: { 
                id: user.id, 
                name: user.name, 
                email: user.email, 
                address: user.address, 
                area: user.area, 
                postalCode: user.postalCode, 
                phoneNumber: user.phoneNumber 
            }
        });
    } catch (err) {
        console.error('PROFILE UPDATE ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//Order Placement
app.post('/api/orders', authMiddleware, async (req, res) => {
    const { productName, quantity, emptyBottle, date, bill, returnDate } = req.body;

    try {
        const user = await User.findById(req.user.id);
        if (!user) return res.status(404).json({ message: 'User not found.' });

        if (user.walletBalance < bill) {
            return res.status(400).json({ message: 'Insufficient wallet balance.' });
        }

        user.walletBalance -= bill;
        await user.save();

        const newOrder = new Order({
            userId: req.user.id,
            productName,
            quantity,
            emptyBottles: emptyBottle,
            deliveryDate: date,
            returnDate: returnDate || null, 
            billAmount: bill,
            status: 'Accepted'
        });
        const order = await newOrder.save();

        res.status(201).json({ message: 'Order placed successfully', order });

    } catch (err) {
        console.error('ORDER PLACEMENT ERROR:', err.message);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//update order status
app.put('/api/orders/update-status', isDriverOrOwner, async (req, res) => {
    const { orderId, status } = req.body;
    
    if (!orderId || !status) {
        return res.status(400).json({ message: 'Order ID and Status are required.' });
    }

    try {
        const order = await Order.findByIdAndUpdate(
            orderId, 
            { status: status }, 
            { new: true }
        );

        if (!order) {
            return res.status(404).json({ message: 'Order not found.' });
        }

        res.json({ message: 'Order status updated successfully', order });
    } catch (err) {
        console.error('UPDATE ORDER STATUS ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//Order Summary (User)
app.get('/api/orders', authMiddleware, async (req, res) => {
    try {
        const orders = await Order.find({ userId: req.user.id }).sort({ createdAt: -1 });
        res.json(orders);
    } catch (err) {
        console.error('GET ORDERS ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//Registered users (Owner)
app.get('/api/users', authMiddleware, async (req, res) => {
    try {
        const users = await User.find({}, 'name email').lean(); 
        
        const today = new Date().toISOString().split('T')[0]; 

        const activeSubscribers = await Subscription.distinct('userId', {
            isActive: true,
            endDate: { $gte: today }
        });

        const subscriberIds = activeSubscribers.map(id => id.toString());

        const usersWithStatus = users.map(user => ({
            ...user,
            isSubscriber: subscriberIds.includes(user._id.toString())
        }));

        res.json(usersWithStatus);
    } catch (err) {
        console.error('GET USERS ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//Order summary (Owner) 
app.get('/api/orders/by-date', authMiddleware, async (req, res) => {
    const { date } = req.query;
    if (!date) return res.status(400).json({ message: 'Date query parameter is required.' });
    
    try {
        const orders = await Order.find({ deliveryDate: date }).populate('userId', 'name email');

        const validOrders = orders.filter(order => order.userId != null);

        const formattedOrders = orders.map(order => ({
            _id: order._id,
            user: order.userId,
            productName: order.productName, 
            quantity: order.quantity,
            emptyBottles: order.emptyBottles,
            deliveryDate: order.deliveryDate,
            billAmount: order.billAmount,
            status: order.status,
            orderType: order.orderType || "Normal" 
        }));
        res.json(formattedOrders);
    } catch (err) {
        console.error('GET ORDERS BY DATE ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//Owner Subscription Dashboard 
app.get('/api/owner/subscriptions', isOwner, async (req, res) => {
    try {
        const report = await Subscription.aggregate([
            { $match: { isActive: true } },

            {
                $lookup: {
                    from: 'users',
                    localField: 'userId',
                    foreignField: '_id',
                    as: 'user'
                }
            },
            { $unwind: '$user' },

            {
                $lookup: {
                    from: 'orders',
                    let: { subUserId: '$userId', subStart: '$startDate', subEnd: '$endDate' },
                    pipeline: [
                        {
                            $match: {
                                $expr: {
                                    $and: [
                                        { $eq: ['$userId', '$$subUserId'] },
                                        { $eq: ['$orderType', 'Subscription'] },
                                        { $gte: ['$deliveryDate', '$$subStart'] },
                                        { $lte: ['$deliveryDate', '$$subEnd'] }
                                    ]
                                }
                            }
                        }
                    ],
                    as: 'subOrders'
                }
            },

            {
                $project: {
                    userName: '$user.name',
                    productName: 1,
                    startDate: 1,
                    endDate: 1,
                    totalOrders: { $size: '$subOrders' },
                    delivered: {
                        $size: {
                            $filter: {
                                input: '$subOrders',
                                as: 'order',
                                cond: { $eq: ['$$order.status', 'Delivered'] }
                            }
                        }
                    },
                    pending: {
                        $size: {
                            $filter: {
                                input: '$subOrders',
                                as: 'order',
                                cond: { $ne: ['$$order.status', 'Delivered'] }
                            }
                        }
                    }
                }
            }
        ]);

        res.json(report);

    } catch (err) {
        console.error('OWNER SUB REPORT ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//User monthly Summary (Owner)
app.get('/api/summary/monthly', authMiddleware, async (req, res) => {
    const { year, month } = req.query;
    if (!year || !month) return res.status(400).json({ message: 'Year and month parameters required.' });
    
    const yearInt = parseInt(year);
    const monthInt = parseInt(month);
    const targetMonthPrefix = `${yearInt}-${String(monthInt).padStart(2, '0')}`;
    const currentDate = new Date().toISOString().split('T')[0];

    try {
        //Get Order Data
        const orderData = await Order.aggregate([
            { $match: { $or: [{ deliveryDate: { $regex: `^${targetMonthPrefix}` } }, { returnDate: { $regex: `^${targetMonthPrefix}` } }] } },
            { $group: { 
                _id: '$userId', 
                totalOrderAmount: { $sum: { $cond: [{ $eq: [{ $substr: ["$deliveryDate", 0, 7] }, targetMonthPrefix] }, "$billAmount", 0] } },
                normalOrdered: { $sum: { $cond: [{ $and: [{ $eq: ["$productName", "Normal Bottle"] }, { $eq: [{ $substr: ["$deliveryDate", 0, 7] }, targetMonthPrefix] }]}, "$quantity", 0] } },
                normalReturned: { $sum: { $cond: [{ $and: [{ $eq: ["$productName", "Normal Bottle"] }, { $eq: [{ $substr: ["$deliveryDate", 0, 7] }, targetMonthPrefix] }]}, "$emptyBottles", 0] } },
                coolerOrdered: { $sum: { $cond: [{ $and: [{ $eq: ["$productName", "Cooler Bottle"] }, { $eq: [{ $substr: ["$deliveryDate", 0, 7] }, targetMonthPrefix] }]}, "$quantity", 0] } },
                coolerReturned: { $sum: { $cond: [{ $and: [{ $eq: ["$productName", "Cooler Bottle"] }, { $ne: ["$returnDate", null] }, { $eq: [{ $substr: ["$returnDate", 0, 7] }, targetMonthPrefix] }, { $lte: ["$returnDate", currentDate] }]}, "$quantity", 0] } },
                box1LOrdered: { $sum: { $cond: [{ $and: [{ $eq: ["$productName", "1 Litre Box"] }, { $eq: [{ $substr: ["$deliveryDate", 0, 7] }, targetMonthPrefix] }]}, "$quantity", 0] } },
                box250mlOrdered: { $sum: { $cond: [{ $and: [{ $eq: ["$productName", "250ml Box"] }, { $eq: [{ $substr: ["$deliveryDate", 0, 7] }, targetMonthPrefix] }]}, "$quantity", 0] } }
            }},
            { $lookup: { from: 'users', localField: '_id', foreignField: '_id', as: 'userDetails' } },
            { $unwind: '$userDetails' },
            { $project: { _id: 1, user: { name: '$userDetails.name', email: '$userDetails.email', phoneNumber: '$userDetails.phoneNumber', address: '$userDetails.address' }, normalOrdered: 1, normalReturned: 1, coolerOrdered: 1, coolerReturned: 1, box1LOrdered: 1, box250mlOrdered: 1, totalOrderAmount: 1 } }
        ]);

        //Get Subscription Data
        const subData = await Subscription.aggregate([
            { $match: { startDate: { $regex: `^${targetMonthPrefix}` } } }, 
            { $group: {
                _id: '$userId',
                totalSubAmount: { $sum: '$totalPrice' },
                lastSubStart: { $last: '$startDate' },
                lastSubEnd: { $last: '$endDate' }
            }}
        ]);

        //Merge Data
        const mergedMap = new Map();
        const initUser = (id) => ({
            userId: id,
            user: { name: 'Unknown', email: '', phoneNumber: '', address: '' },
            normalOrdered: 0, normalReturned: 0, coolerOrdered: 0, coolerReturned: 0, box1LOrdered: 0, box250mlOrdered: 0,
            totalOrderAmount: 0,
            subscriptionBill: 0,
            subStartDate: null,
            subEndDate: null,
            totalAmount: 0
        });

        orderData.forEach(item => {
            const id = item._id.toString();
            if (!mergedMap.has(id)) mergedMap.set(id, initUser(id));
            const entry = mergedMap.get(id);
            Object.assign(entry, item); 
            entry.totalAmount += item.totalOrderAmount;
        });

        for (const sub of subData) {
            const id = sub._id.toString();
            if (!mergedMap.has(id)) {
                const userInfo = await User.findById(id).select('name email phoneNumber address');
                const entry = initUser(id);
                if(userInfo) {
                    entry.user = { name: userInfo.name, email: userInfo.email, phoneNumber: userInfo.phoneNumber, address: userInfo.address };
                }
                mergedMap.set(id, entry);
            }
            const entry = mergedMap.get(id);
            entry.subscriptionBill = sub.totalSubAmount;
            entry.subStartDate = sub.lastSubStart;
            entry.subEndDate = sub.lastSubEnd;
            entry.totalAmount += sub.totalSubAmount; 
        }

        const userSummaries = Array.from(mergedMap.values());
        const grandTotalAmount = userSummaries.reduce((sum, current) => sum + current.totalAmount, 0);

        res.json({ grandTotalAmount, userSummaries });

    } catch (err) {
        console.error('GET MONTHLY SUMMARY ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//Order Details (Delivery Guy)
app.get('/api/deliveries/by-date', isDriverOrOwner, async (req, res) => {
    const { date } = req.query; 
    if (!date) return res.status(400).json({ message: 'Date query parameter is required.' });

    try {
        const orders = await Order.find({
            $or: [
                { deliveryDate: date },
                { returnDate: date }
            ]
        }).populate('userId', 'name address phoneNumber area');

        const tasks = [];
        
        const driverArea = req.user.role === 'driver' ? req.user.area : null;

        orders.forEach(order => {
            if (!order.userId) return; 

            if (driverArea && order.userId.area !== driverArea) {
                return; 
            }

            if (order.deliveryDate === date) {
                tasks.push({
                    _id: order._id,
                    taskId: order._id + "_DELIVERY",
                    type: 'Delivery', 
                    productName: order.productName,
                    quantity: order.quantity,
                    emptyBottles: order.emptyBottles,
                    orderType: order.orderType || "Normal",
                    status: order.status, 
                    userId: {
                        name: order.userId.name,
                        address: order.userId.address,
                        phoneNumber: order.userId.phoneNumber,
                        area: order.userId.area 
                    }
                });
            }

            if (order.returnDate === date) {
                tasks.push({
                    _id: order._id,
                    taskId: order._id + "_PICKUP",
                    type: 'Pickup', 
                    productName: order.productName,
                    quantity: order.quantity, 
                    emptyBottles: 0, 
                    orderType: order.orderType || "Normal",
                    status: order.status,
                    userId: {
                        name: order.userId.name,
                        address: order.userId.address,
                        phoneNumber: order.userId.phoneNumber,
                        area: order.userId.area
                    }
                });
            }
        });

        res.json(tasks);

    } catch (err) {
        console.error('GET DELIVERIES ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//see current balance
app.get('/api/wallet/balance', authMiddleware, async (req, res) => {
    try {
        const user = await User.findById(req.user.id);
        if (!user) return res.status(404).json({ message: 'User not found' });
        res.status(200).json({ balance: user.walletBalance });
    } catch (err) {
        console.error('GET WALLET BALANCE ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//add money in wallet
app.post('/api/wallet/add', authMiddleware, async (req, res) => {
    const { amount } = req.body;
    if (!amount || amount <= 0) return res.status(400).json({ message: 'Please provide a valid amount.' });

    try {
        const updatedUser = await User.findOneAndUpdate(
            { _id: req.user.id },
            { $inc: { walletBalance: Number(amount) } },
            { new: true } 
        );

        if (!updatedUser) return res.status(404).json({ message: 'User not found' });

        const newTransaction = new Transaction({
            userId: req.user.id,
            amount: Number(amount),
            type: 'Credit',
            description: 'Added to Wallet'
        });
        await newTransaction.save();

        res.status(200).json({ 
            message: 'Amount added successfully', 
            newBalance: updatedUser.walletBalance 
        });
    } catch (err) {
        console.error('ADD TO WALLET ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//wallet history
app.get('/api/wallet/history', authMiddleware, async (req, res) => {
    try {
        const transactions = await Transaction.find({ userId: req.user.id }).sort({ createdAt: -1 });
        res.json(transactions);
    } catch (err) {
        console.error('GET WALLET HISTORY ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//Send a new announcement (Owner)
app.post('/api/announcements', isOwner, async (req, res) => {
    const { message } = req.body;
    if (!message) return res.status(400).json({ message: 'Announcement message is required.' });
    try {
        const newAnnouncement = new Announcement({ message });
        await newAnnouncement.save();
        res.status(201).json({ message: 'Announcement sent successfully.' });
    } catch (err) {
        console.error('ANNOUNCEMENT ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});

//Get all announcements (user/driver/owner)
app.get('/api/announcements', authMiddleware, async (req, res) => {
    try {
        const announcements = await Announcement.find().sort({ createdAt: -1 });
        res.json(announcements);
    } catch (err) {
        console.error('GET ANNOUNCEMENTS ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});

//Get count of new announcements 
app.get('/api/announcements/new-count', authMiddleware, async (req, res) => {
    try {
        if (!mongoose.Types.ObjectId.isValid(req.user.id)) {
            return res.json({ newCount: 0 });
        }

        const user = await User.findById(req.user.id).select('lastReadAnnouncements');
        if (!user) return res.status(404).json({ message: 'User not found' });
        
        const count = await Announcement.countDocuments({
            createdAt: { $gt: user.lastReadAnnouncements }
        });
        res.json({ newCount: count });
    } catch (err) {
        console.error('GET NEW COUNT ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});

//Mark all announcements as read
app.post('/api/announcements/read', authMiddleware, async (req, res) => {
    try {
        if (!mongoose.Types.ObjectId.isValid(req.user.id)) {
            return res.status(200).json({ message: 'Skipped for static user.' });
        }

        await User.findByIdAndUpdate(req.user.id, { lastReadAnnouncements: Date.now() });
        res.status(200).json({ message: 'Announcements marked as read.' });
    } catch (err) {
        console.error('MARK READ ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


//Corn job(autumatically place order)
// cron.schedule('0 * * * *', async () => {
//     console.log('Running Hourly Subscription Check...');
//     await processSubscriptions();
// });

// const processSubscriptions = async () => {
   
//     const now = new Date();
//     const year = now.getFullYear();
//     const month = String(now.getMonth() + 1).padStart(2, '0');
//     const day = String(now.getDate()).padStart(2, '0');
    
//     const todayStr = `${year}-${month}-${day}`;
    
//     console.log(`Checking subscriptions for date: ${todayStr}`);

//     try {
//         const activeSubs = await Subscription.find({
//             isActive: true,
//             startDate: { $lte: todayStr },
//             endDate: { $gte: todayStr }
//         });

//         console.log(`Found ${activeSubs.length} active subscriptions.`);

//         for (const sub of activeSubs) {
//             const existingOrder = await Order.findOne({
//                 userId: sub.userId,
//                 deliveryDate: todayStr,
//                 orderType: 'Subscription'
//             });

//             if (!existingOrder) {
//                 const newOrder = new Order({
//                     userId: sub.userId,
//                     productName: sub.productName,
//                     quantity: sub.quantity,
                    
//                     emptyBottles: sub.quantity,
                    
//                     deliveryDate: todayStr,
//                     billAmount: 0, 
//                     orderType: 'Subscription',
//                     status: 'Accepted'
//                 });
//                 await newOrder.save();
//                 console.log(`[SUCCESS] Auto-order placed for user ${sub.userId}`);
//             } else {
//                 console.log(`[SKIP] Order already exists for user ${sub.userId}`);
//             }
//         }
//     } catch (err) {
//         console.error('CRON JOB ERROR:', err);
//     }
// };
cron.schedule('1 0 * * *', async () => {
    console.log('Running Daily Subscription Check at 12:01 AM IST...');
    await processSubscriptions();
}, {
    scheduled: true,
    timezone: "Asia/Kolkata" 
});

const processSubscriptions = async () => {
    const now = new Date();
    now.setUTCHours(now.getUTCHours() + 5);
    now.setUTCMinutes(now.getUTCMinutes() + 30);
    
    const year = now.getUTCFullYear();
    const month = String(now.getUTCMonth() + 1).padStart(2, '0');
    const day = String(now.getUTCDate()).padStart(2, '0');
    
    const todayStr = `${year}-${month}-${day}`;
    
    console.log(`Checking subscriptions for IST date: ${todayStr}`);

    try {
        const activeSubs = await Subscription.find({
            isActive: true,
            startDate: { $lte: todayStr },
            endDate: { $gte: todayStr }
        });

        console.log(`Found ${activeSubs.length} active subscriptions.`);

        for (const sub of activeSubs) {
            const existingOrder = await Order.findOne({
                userId: sub.userId,
                deliveryDate: todayStr,
                orderType: 'Subscription'
            });

            if (!existingOrder) {
                const newOrder = new Order({
                    userId: sub.userId,
                    productName: sub.productName,
                    quantity: sub.quantity,
                    emptyBottles: sub.quantity, 
                    deliveryDate: todayStr,
                    billAmount: 0, 
                    orderType: 'Subscription',
                    status: 'Accepted'
                });
                await newOrder.save();
                console.log(`[SUCCESS] Auto-order placed for user ${sub.userId}`);
            } else {
                console.log(`[SKIP] Order already exists for user ${sub.userId}`);
            }
        }
    } catch (err) {
        console.error('CRON JOB ERROR:', err);
    }
};




//Buy Subscription With Carry Forward Undelivered bottles 
app.post('/api/subscription/create', authMiddleware, async (req, res) => {
    const { productName, quantity, startDate, pricePerUnit } = req.body;

    if (!productName || !quantity || !startDate || !pricePerUnit) {
        return res.status(400).json({ message: 'All fields are required' });
    }

    try {
        const user = await User.findById(req.user.id);
        if (!user) return res.status(404).json({ message: 'User not found' });

        const standardDays = 30;
        const totalCost = standardDays * quantity * pricePerUnit;

        if (user.walletBalance < totalCost) {
            return res.status(400).json({ message: `Insufficient balance. Required: ₹${totalCost}` });
        }

        const lastSub = await Subscription.findOne({ userId: req.user.id }).sort({ createdAt: -1 });
        let extraDays = 0;
        let carryForwardMsg = "";

        if (lastSub) {
            const lsStart = new Date(lastSub.startDate);
            const lsEnd = new Date(lastSub.endDate);
            const durationDays = Math.round((lsEnd - lsStart) / (1000 * 60 * 60 * 24)) + 1;
            
            const expectedTotal = durationDays * lastSub.quantity;

            const deliveredCount = await Order.countDocuments({
                userId: req.user.id,
                orderType: 'Subscription',
                deliveryDate: { $gte: lastSub.startDate, $lte: lastSub.endDate },
                status: 'Delivered'
            });

            const missedBottles = expectedTotal - deliveredCount;

            if (missedBottles > 0) {
                extraDays = Math.ceil(missedBottles / quantity);
                carryForwardMsg = ` (Added ${extraDays} extra days for ${missedBottles} pending bottles)`;
            }
        }

        const totalDuration = standardDays + extraDays;
        
        const start = new Date(startDate);
        const end = new Date(start);
        end.setDate(start.getDate() + (totalDuration - 1)); 
        
        const endDate = end.toISOString().split('T')[0];

        
        user.walletBalance -= totalCost;
        await user.save();

        const newSub = new Subscription({
            userId: req.user.id,
            productName,
            quantity,
            startDate,
            endDate,
            totalPrice: totalCost
        });
        await newSub.save();
       
        const today = new Date().toISOString().split('T')[0];
        if (startDate === today) {
            const firstOrder = new Order({
                userId: req.user.id,
                productName,
                quantity,
                emptyBottles: quantity,
                deliveryDate: startDate,
                billAmount: 0,
                orderType: 'Subscription',
                status: 'Accepted'
            });
            await firstOrder.save();
        }

        res.status(201).json({ 
            message: 'Subscription purchased successfully!' + carryForwardMsg, 
            subscription: newSub 
        });

    } catch (err) {
        console.error('SUBSCRIPTION ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});


// Get Active Subscription 
app.get('/api/subscription/status', authMiddleware, async (req, res) => {
    try {
        const today = new Date().toISOString().split('T')[0];
        const sub = await Subscription.findOne({ 
            userId: req.user.id, 
            isActive: true,
            endDate: { $gte: today } 
        }).sort({ createdAt: -1 });

        res.json(sub); 
    } catch (err) {
        console.error('GET SUB ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});

app.get('/api/subscription/preview', authMiddleware, async (req, res) => {
    try {
        const lastSub = await Subscription.findOne({ userId: req.user.id }).sort({ createdAt: -1 });
        let missedBottles = 0;

        if (lastSub) {
            const lsStart = new Date(lastSub.startDate);
            const lsEnd = new Date(lastSub.endDate);
            const durationDays = Math.round((lsEnd - lsStart) / (1000 * 60 * 60 * 24)) + 1;
            
            const expectedTotal = durationDays * lastSub.quantity;

            const deliveredCount = await Order.countDocuments({
                userId: req.user.id,
                orderType: 'Subscription',
                deliveryDate: { $gte: lastSub.startDate, $lte: lastSub.endDate },
                status: 'Delivered'
            });

            missedBottles = Math.max(0, expectedTotal - deliveredCount);
        }
        res.json({ missedBottles });
    } catch (err) {
        console.error('SUB PREVIEW ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});

//get subscription details
app.get('/api/subscription/details', authMiddleware, async (req, res) => {
    try {
        const today = new Date().toISOString().split('T')[0];
        
        const sub = await Subscription.findOne({ 
            userId: req.user.id, 
            isActive: true,
            endDate: { $gte: today } 
        }).sort({ createdAt: -1 });

        if (!sub) {
            return res.status(404).json({ message: "No active subscription found" });
        }

        const orders = await Order.find({
            userId: req.user.id,
            orderType: 'Subscription',
            deliveryDate: { $gte: sub.startDate, $lte: sub.endDate }
        });

        const totalGenerated = orders.length;
        const delivered = orders.filter(o => o.status === 'Delivered').length;
        const pending = orders.filter(o => o.status === 'Accepted' || o.status === 'Pending').length;

        res.json({
            subscription: sub,
            stats: {
                totalGenerated,
                delivered,
                pending
            }
        });
    } catch (err) {
        console.error('GET SUB DETAILS ERROR:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});

// test through postman(automatic order)
app.get('/api/test/run-subscriptions', async (req, res) => {
    try {
        await processSubscriptions();
        res.json({ message: 'Subscription check ran manually. Check server console for details.' });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});


const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server is running on port ${PORT}`);
});