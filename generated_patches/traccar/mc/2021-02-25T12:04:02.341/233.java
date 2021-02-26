package org.traccar.protocol;

import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.traccar.BaseProtocolDecoder;
import org.traccar.helper.Crc;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Event;
import org.traccar.model.Position;

public class CastelProtocolDecoder extends BaseProtocolDecoder {

    public CastelProtocolDecoder(String protocol) {
        super(protocol);
    }

    private static final short MSG_LOGIN = 0x1001;

    private static final short MSG_LOGIN_RESPONSE = (short) 0x9001;

    private static final short MSG_HEARTBEAT = 0x1003;

    private static final short MSG_HEARTBEAT_RESPONSE = (short) 0x9003;

    private static final short MSG_GPS = 0x4001;

    @Override
protected Object decode(ChannelHandlerContext ctx, Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
    ChannelBuffer buf = (ChannelBuffer) msg;
    // header
buf.skipBytes(2);
    // length
buf.readUnsignedShort();
    int version = buf.readUnsignedByte();
    ChannelBuffer id = buf.readBytes(20);
    int type = ChannelBuffers.swapShort(buf.readShort());
    if (type == MSG_HEARTBEAT) {
        if (channel != null) {
            ChannelBuffer response = ChannelBuffers.directBuffer(ByteOrder.LITTLE_ENDIAN, 31);
            response.writeByte(0x40);
            response.writeByte(0x40);
            response.writeShort(response.capacity());
            response.writeByte(version);
            response.writeBytes(id);
            response.writeShort(ChannelBuffers.swapShort(MSG_HEARTBEAT_RESPONSE));
            response.writeShort(Crc.crc16Ccitt(response.toByteBuffer(0, response.writerIndex())));
            response.writeByte(0x0D);
            response.writeByte(0x0A);
            channel.write(response, remoteAddress);
        }
    } else if (type == MSG_LOGIN || type == MSG_GPS) {
        Position position = new Position();
        position.setDeviceId(getDeviceId());
        position.setProtocol(getProtocol());
        if (!identify(id.toString(Charset.defaultCharset()))) {
            return null;
        } else if (type == MSG_LOGIN) {
            if (channel == null) {
                ChannelBuffer response = ChannelBuffers.directBuffer(ByteOrder.LITTLE_ENDIAN, 41);
                response.writeByte(0x40);
                response.writeByte(0x40);
                response.writeShort(response.capacity());
                response.writeByte(version);
                response.writeBytes(id);
                response.writeShort(ChannelBuffers.swapShort(MSG_LOGIN_RESPONSE));
                response.writeInt(0xFFFFFFFF);
                response.writeShort(0);
                response.writeInt((int) (new Date().getTime() / 1000));
                response.writeShort(Crc.crc16Ccitt(response.toByteBuffer(0, response.writerIndex())));
                response.writeByte(0x0D);
                response.writeByte(0x0A);
                channel.write(response, remoteAddress);
            }
        }
        if (type == MSG_GPS) {
            // historical
buf.readUnsignedByte();
        }
        // ACC ON time
buf.readUnsignedInt();
        // UTC time
buf.readUnsignedInt();
        position.set(Event.KEY_ODOMETER, buf.readUnsignedInt());
        // trip odometer
buf.readUnsignedInt();
        // total fuel consumption
buf.readUnsignedInt();
        // current fuel consumption
buf.readUnsignedShort();
        position.set(Event.KEY_STATUS, buf.readUnsignedInt());
        buf.skipBytes(8);
        // count
buf.readUnsignedByte();
        // Date and time
Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        time.clear();
        time.set(Calendar.DAY_OF_MONTH, buf.readUnsignedByte());
        time.set(buf.readUnsignedByte() - 1, 0, 0, 0, Calendar.MONTH);
        time.set(Calendar.YEAR, 2000 + buf.readUnsignedByte());
        time.set(Calendar.HOUR_OF_DAY, buf.readUnsignedByte());
        time.set(Calendar.MINUTE, buf.readUnsignedByte());
        time.set(Calendar.SECOND, buf.readUnsignedByte());
        position.setTime(time.getTime());
        double lat = buf.readUnsignedInt() / 3600000.0;
        double lon = buf.readUnsignedInt() / 3600000.0;
        position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));
        position.setCourse(buf.readUnsignedShort());
        int flags = buf.readUnsignedByte();
        position.setLatitude((flags & 0x01) == 0 ? -lat : lat);
        position.setLongitude((flags & 0x02) == 0 ? -lon : lon);
        position.setValid((flags & 0x0C) > 0);
        position.set(Event.KEY_SATELLITES, flags >> 4);
        return position;
    }
    return null;
}
}
