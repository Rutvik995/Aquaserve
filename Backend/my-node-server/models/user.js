const mongoose = require('mongoose');
const UserSchema = new mongoose.Schema({
    name: {
        type: String,
        required: [true, 'Please provide a name']
    },
    email: {
        type: String,
        required: [true, 'Please provide an email'],
        unique: true,
        match: [
            /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/,
            'Please provide a valid email'
        ]
    },
    password: {
        type: String,
        required: [true, 'Please provide a password'],
        minlength: 8
    },
    phoneNumber: {
        type: String,
        default: '' 
    },
    address: {
        type: String,
        default: ''
    },
    area: {
        type:String,
        defalut: ''
    },
    postalCode: {
        type: String,
        default: ''
    },
    extra: {
        type: String,
        default: ''
    },
    lastReadAnnouncements: {
        type: Date,
        default: Date.now
    },
    walletBalance: { 
        type: Number,
        required: true,
        default: 0
    },
    resetPasswordToken: {
        type: String,
        default: undefined
    },
    resetPasswordExpires: {
        type: Date,
        default: undefined
    }
}, {
    timestamps: true 
});


module.exports = mongoose.model('User', UserSchema);