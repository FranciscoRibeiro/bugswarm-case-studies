/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.protocol;

import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.util.Date;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.traccar.BaseProtocolDecoder;
import org.traccar.helper.Checksum;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Event;
import org.traccar.model.Position;

public class NavigilProtocolDecoder extends BaseProtocolDecoder {

    public NavigilProtocolDecoder(NavigilProtocol protocol) {
        super(protocol);
    }

    private static final int LEAP_SECONDS_DELTA = 25;

    public static final int MSG_ERROR = 2;
    public static final int MSG_INDICATION = 4;
    public static final int MSG_CONN_OPEN = 5;
    public static final int MSG_CONN_CLOSE = 6;
    public static final int MSG_SYSTEM_REPORT = 7;
    public static final int MSG_UNIT_REPORT = 8;
    public static final int MSG_GEOFENCE_ALARM = 10;
    public static final int MSG_INPUT_ALARM = 11;
    public static final int MSG_TG2_REPORT = 12;
    public static final int MSG_POSITION_REPORT = 13;
    public static final int MSG_POSITION_REPORT_2 = 15;
    public static final int MSG_SNAPSHOT4 = 17;
    public static final int MSG_TRACKING_DATA = 18;
    public static final int MSG_MOTION_ALARM = 19;
    public static final int MSG_ACKNOWLEDGEMENT = 255;

    private static Date convertTimestamp(long timestamp) {
        return new Date((timestamp - LEAP_SECONDS_DELTA) * 1000l);
    }

    private int senderSequenceNumber = 1;

    private void sendAcknowledgment(Channel channel, int sequenceNumber) {
        ChannelBuffer data = ChannelBuffers.directBuffer(ByteOrder.LITTLE_ENDIAN, 4);
        data.writeShort(sequenceNumber);
        data.writeShort(0); // OK

        ChannelBuffer header = ChannelBuffers.directBuffer(ByteOrder.LITTLE_ENDIAN, 20);
        header.writeByte(1); header.writeByte(0);
        header.writeShort(senderSequenceNumber++);
        header.writeShort(MSG_ACKNOWLEDGEMENT);
        header.writeShort(header.capacity() + data.capacity());
        header.writeShort(0);
        header.writeShort(Checksum.crc16(Checksum.CRC16_CCITT_FALSE, data.toByteBuffer()));
        header.writeInt(0);
        header.writeInt((int) (System.currentTimeMillis() / 1000) + LEAP_SECONDS_DELTA);

        if (channel != null) {
            channel.write(ChannelBuffers.copiedBuffer(header, data));
        }
    }

    private Position parseUnitReport(ChannelBuffer buf, int sequenceNumber) {
        Position position = new Position();
        position.setProtocol(getProtocolName());

        position.setValid(true);
        position.set(Event.KEY_INDEX, sequenceNumber);
        position.setDeviceId(getDeviceId());

        buf.readUnsignedShort(); // report trigger
        buf.readUnsignedShort(); // flags

        position.setLatitude(buf.readInt() * 0.0000001);
        position.setLongitude(buf.readInt() * 0.0000001);
        position.setAltitude(buf.readUnsignedShort());

        buf.readUnsignedShort(); // satellites in fix
        buf.readUnsignedShort(); // satellites in track
        buf.readUnsignedShort(); // GPS antenna state

        position.setSpeed(buf.readUnsignedShort() * 0.194384);
        position.setCourse(buf.readUnsignedShort());

        buf.readUnsignedInt(); // distance
        buf.readUnsignedInt(); // delta distance

        position.set(Event.KEY_BATTERY, buf.readUnsignedShort() * 0.001);

        buf.readUnsignedShort(); // battery charger status

        position.setTime(convertTimestamp(buf.readUnsignedInt()));

        // TODO: a lot of other stuff
        return position;
    }

    private Position parseTg2Report(ChannelBuffer buf, int sequenceNumber) {
        Position position = new Position();
        position.setProtocol(getProtocolName());

        position.setValid(true);
        position.set(Event.KEY_INDEX, sequenceNumber);
        position.setDeviceId(getDeviceId());

        buf.readUnsignedShort(); // report trigger
        buf.readUnsignedByte(); // reserved
        buf.readUnsignedByte(); // assisted GPS age

        position.setTime(convertTimestamp(buf.readUnsignedInt()));

        position.setLatitude(buf.readInt() * 0.0000001);
        position.setLongitude(buf.readInt() * 0.0000001);
        position.setAltitude(buf.readUnsignedShort());

        buf.readUnsignedByte(); // satellites in fix
        buf.readUnsignedByte(); // satellites in track

        position.setSpeed(buf.readUnsignedShort() * 0.194384);
        position.setCourse(buf.readUnsignedShort());

        buf.readUnsignedInt(); // distance
        buf.readUnsignedShort(); // maximum speed
        buf.readUnsignedShort(); // minimum speed

        buf.readUnsignedShort(); // VSAUT1 voltage
        buf.readUnsignedShort(); // VSAUT2 voltage
        buf.readUnsignedShort(); // solar voltage
        position.set(Event.KEY_BATTERY, buf.readUnsignedShort() * 0.001);

        // TODO: a lot of other stuff
        return position;
    }

    private Position parsePositionReport(ChannelBuffer buf, int sequenceNumber, long timestamp) {
        Position position = new Position();
        position.setProtocol(getProtocolName());

        position.set(Event.KEY_INDEX, sequenceNumber);
        position.setDeviceId(getDeviceId());
        position.setTime(convertTimestamp(timestamp));

        position.setLatitude(buf.readMedium() * 0.00002);
        position.setLongitude(buf.readMedium() * 0.00002);

        position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
        position.setCourse(buf.readUnsignedByte() * 2);

        short flags = buf.readUnsignedByte();
        position.setValid((flags & 0x80) == 0x80 && (flags & 0x40) == 0x40);

        buf.readUnsignedByte(); // reserved
        return position;
    }

    private Position parsePositionReport2(ChannelBuffer buf, int sequenceNumber, long timestamp) {
        Position position = new Position();
        position.setProtocol(getProtocolName());

        position.set(Event.KEY_INDEX, sequenceNumber);
        position.setDeviceId(getDeviceId());
        position.setTime(convertTimestamp(timestamp));

        position.setLatitude(buf.readInt() * 0.0000001);
        position.setLongitude(buf.readInt() * 0.0000001);

        buf.readUnsignedByte(); // report trigger

        position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));

        short flags = buf.readUnsignedByte();
        position.setValid((flags & 0x80) == 0x80 && (flags & 0x40) == 0x40);

        position.set(Event.KEY_SATELLITES, buf.readUnsignedByte());
        buf.readUnsignedInt(); // distance
        return position;
    }

    private Position parseSnapshot4(ChannelBuffer buf, int sequenceNumber) {
        Position position = new Position();
        position.setProtocol(getProtocolName());

        position.set(Event.KEY_INDEX, sequenceNumber);
        position.setDeviceId(getDeviceId());

        buf.readUnsignedByte(); // report trigger
        buf.readUnsignedByte(); // position fix source
        buf.readUnsignedByte(); // GNSS fix quality
        buf.readUnsignedByte(); // GNSS assistance age

        long flags = buf.readUnsignedInt();
        position.setValid((flags & 0x0400) == 0x0400);

        position.setTime(convertTimestamp(buf.readUnsignedInt()));

        position.setLatitude(buf.readInt() * 0.0000001);
        position.setLongitude(buf.readInt() * 0.0000001);
        position.setAltitude(buf.readUnsignedShort());

        buf.readUnsignedByte(); // satellites in fix
        buf.readUnsignedByte(); // satellites in track

        position.setSpeed(buf.readUnsignedShort() * 0.194384);
        position.setCourse(buf.readUnsignedShort() * 0.1);

        buf.readUnsignedByte(); // maximum speed
        buf.readUnsignedByte(); // minimum speed
        buf.readUnsignedInt(); // distance

        buf.readUnsignedByte(); // supply voltage 1
        buf.readUnsignedByte(); // supply voltage 2
        position.set(Event.KEY_BATTERY, buf.readUnsignedShort() * 0.001);

        // TODO: a lot of other stuff
        return position;
    }

    private Position parseTrackingData(ChannelBuffer buf, int sequenceNumber, long timestamp) {
        Position position = new Position();
        position.setProtocol(getProtocolName());

        position.set(Event.KEY_INDEX, sequenceNumber);
        position.setDeviceId(getDeviceId());
        position.setTime(convertTimestamp(timestamp));

        buf.readUnsignedByte(); // tracking mode

        short flags = buf.readUnsignedByte();
        position.setValid((flags & 0x01) == 0x01);

        buf.readUnsignedShort(); // duration

        position.setLatitude(buf.readInt() * 0.0000001);
        position.setLongitude(buf.readInt() * 0.0000001);

        position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
        position.setCourse(buf.readUnsignedByte() * 2.0);

        buf.readUnsignedByte(); // satellites in fix

        position.set(Event.KEY_BATTERY, buf.readUnsignedShort() * 0.001);

        buf.readUnsignedInt(); // distance
        return position;
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg)
            throws Exception {

        ChannelBuffer buf = (ChannelBuffer) msg;

        buf.readUnsignedByte(); // protocol version
        buf.readUnsignedByte(); // version id
        int sequenceNumber = buf.readUnsignedShort();
        int messageId = buf.readUnsignedShort();
        buf.readUnsignedShort(); // length
        int flags = buf.readUnsignedShort();
        buf.readUnsignedShort(); // checksum

        // Get device identifier
        if (!identify(String.valueOf(buf.readUnsignedInt()), channel)) {
            return null;
        }

        long timestamp = buf.readUnsignedInt(); // message timestamp

        // Acknowledgment
        if ((flags & 0x1) == 0x0) {
            sendAcknowledgment(channel, sequenceNumber);
        }

        // Parse messages
        switch (messageId) {
            case MSG_UNIT_REPORT:
                return parseUnitReport(buf, sequenceNumber);
            case MSG_TG2_REPORT:
                return parseTg2Report(buf, sequenceNumber);
            case MSG_POSITION_REPORT:
                return parsePositionReport(buf, sequenceNumber, timestamp);
            case MSG_POSITION_REPORT_2:
                return parsePositionReport2(buf, sequenceNumber, timestamp);
            case MSG_SNAPSHOT4:
                return parseSnapshot4(buf, sequenceNumber);
            case MSG_TRACKING_DATA:
                return parseTrackingData(buf, sequenceNumber, timestamp);
        }

        return null;
    }

}
