package net.minecraft.server.v1_12_R1;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Collection;

public class PacketPlayOutScoreboardTeam implements Packet<PacketListenerPlayOut> {
    private String a = "";

    private String b = "";

    private String c = "";

    private String d = "";

    private String e = ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS.e;

    private String f = ScoreboardTeamBase.EnumTeamPush.ALWAYS.e;

    private int g = -1;

    private final Collection<String> h = Lists.newArrayList();

    private int i;

    private int j;

    public PacketPlayOutScoreboardTeam(ScoreboardTeam paramScoreboardTeam, int paramInt) {
        this.a = paramScoreboardTeam.getName();
        this.i = paramInt;
        if (paramInt == 0 || paramInt == 2) {
            this.b = paramScoreboardTeam.getDisplayName();
            this.c = paramScoreboardTeam.getPrefix();
            this.d = paramScoreboardTeam.getSuffix();
            this.j = paramScoreboardTeam.packOptionData();
            this.e = (paramScoreboardTeam.getNameTagVisibility()).e;
            this.f = (paramScoreboardTeam.getCollisionRule()).e;
            this.g = paramScoreboardTeam.getColor().b();
        }
        if (paramInt == 0)
            this.h.addAll(paramScoreboardTeam.getPlayerNameSet());
    }

    public PacketPlayOutScoreboardTeam(ScoreboardTeam paramScoreboardTeam, Collection<String> paramCollection, int paramInt) {
        try{
            if (paramInt != 3 && paramInt != 4)
                throw new IllegalArgumentException("Method must be join or leave for player constructor");
            if (paramCollection == null || paramCollection.isEmpty())
                throw new IllegalArgumentException("Players cannot be null/empty");
            this.i = paramInt;
            this.a = paramScoreboardTeam.getName();
            this.h.addAll(paramCollection);
        }catch (Exception e){

        }

    }

    public void a(PacketDataSerializer paramPacketDataSerializer) throws IOException {
        this.a = paramPacketDataSerializer.e(16);
        this.i = paramPacketDataSerializer.readByte();
        if (this.i == 0 || this.i == 2) {
            this.b = paramPacketDataSerializer.e(32);
            this.c = paramPacketDataSerializer.e(16);
            this.d = paramPacketDataSerializer.e(16);
            this.j = paramPacketDataSerializer.readByte();
            this.e = paramPacketDataSerializer.e(32);
            this.f = paramPacketDataSerializer.e(32);
            this.g = paramPacketDataSerializer.readByte();
        }
        if (this.i == 0 || this.i == 3 || this.i == 4) {
            int i = paramPacketDataSerializer.g();
            for (byte b = 0; b < i; b++)
                this.h.add(paramPacketDataSerializer.e(40));
        }
    }

    public void b(PacketDataSerializer paramPacketDataSerializer) throws IOException {
        paramPacketDataSerializer.a(this.a);
        paramPacketDataSerializer.writeByte(this.i);
        if (this.i == 0 || this.i == 2) {
            paramPacketDataSerializer.a(this.b);
            paramPacketDataSerializer.a(this.c);
            paramPacketDataSerializer.a(this.d);
            paramPacketDataSerializer.writeByte(this.j);
            paramPacketDataSerializer.a(this.e);
            paramPacketDataSerializer.a(this.f);
            paramPacketDataSerializer.writeByte(this.g);
        }
        if (this.i == 0 || this.i == 3 || this.i == 4) {
            paramPacketDataSerializer.d(this.h.size());
            for (String str : this.h)
                paramPacketDataSerializer.a(str);
        }
    }

    public void a(PacketListenerPlayOut paramPacketListenerPlayOut) {
        paramPacketListenerPlayOut.a(this);
    }

    public PacketPlayOutScoreboardTeam() {}
}