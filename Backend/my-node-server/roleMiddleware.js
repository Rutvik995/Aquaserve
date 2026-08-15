const jwt = require('jsonwebtoken');

const isDriverOrOwner = (req, res, next) => {
    const authHeader = req.header('Authorization');
    if (!authHeader) {
        return res.status(401).json({ msg: 'No token, authorization denied' });
    }

    const token = authHeader.split(' ')[1];
    if (!token) {
        return res.status(401).json({ msg: 'Token format is invalid, authorization denied' });
    }

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        
        if (decoded.user.role === 'driver' || decoded.user.role === 'owner') {
            req.user = decoded.user;
            next(); 
        } else {
            return res.status(403).json({ msg: 'Forbidden: Access is denied' });
        }
    } catch (err) {
        res.status(401).json({ msg: 'Token is not valid' });
    }
};

const isOwner = (req, res, next) => {
    const authHeader = req.header('Authorization');
    if (!authHeader) {
        return res.status(401).json({ msg: 'No token, authorization denied' });
    }

    const token = authHeader.split(' ')[1];
    if (!token) {
        return res.status(401).json({ msg: 'Token format is invalid, authorization denied' });
    }

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        
        if (decoded.user.role === 'owner') {
            req.user = decoded.user;
            next(); 
        } else {
            return res.status(403).json({ msg: 'Forbidden: Access is denied' });
        }
    } catch (err) {
        res.status(401).json({ msg: 'Token is not valid' });
    }
};

module.exports = { isDriverOrOwner, isOwner };