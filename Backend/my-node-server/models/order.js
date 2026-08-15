const mongoose = require('mongoose');

const orderSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    productName: { type: String, required: true },
    quantity: { type: Number, required: true },
    emptyBottles: { type: Number, required: true },
    deliveryDate: { type: String, required: true },
    returnDate: { type: String, default: null }, 
    billAmount: { type: Number},
    status: { type: String, default: 'Accepted' },
    orderType: { type: String, enum: ['Normal', 'Subscription'], default: 'Normal' },
    createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Order', orderSchema);