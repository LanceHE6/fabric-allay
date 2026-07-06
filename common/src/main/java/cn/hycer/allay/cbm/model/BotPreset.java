package cn.hycer.allay.cbm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class BotPreset {

    private String name;
    private String description;
    private String dimension;
    private double x, y, z;
    private float yaw, pitch;

    @JsonProperty("look_x")
    @SerializedName("look_x")
    private double lookX;

    @JsonProperty("look_y")
    @SerializedName("look_y")
    private double lookY;

    @JsonProperty("look_z")
    @SerializedName("look_z")
    private double lookZ;

    public BotPreset() {}

    public BotPreset(String name, String description, String dimension, double x, double y, double z,
                     float yaw, float pitch, double lookX, double lookY, double lookZ) {
        this.name = name; this.description = description; this.dimension = dimension;
        this.x = x; this.y = y; this.z = z;
        this.yaw = yaw; this.pitch = pitch;
        this.lookX = lookX; this.lookY = lookY; this.lookZ = lookZ;
    }

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getDimension() { return dimension; }
    public void setDimension(String v) { this.dimension = v; }
    public double getX() { return x; }
    public void setX(double v) { this.x = v; }
    public double getY() { return y; }
    public void setY(double v) { this.y = v; }
    public double getZ() { return z; }
    public void setZ(double v) { this.z = v; }
    public float getYaw() { return yaw; }
    public void setYaw(float v) { this.yaw = v; }
    public float getPitch() { return pitch; }
    public void setPitch(float v) { this.pitch = v; }
    public double getLookX() { return lookX; }
    public void setLookX(double v) { this.lookX = v; }
    public double getLookY() { return lookY; }
    public void setLookY(double v) { this.lookY = v; }
    public double getLookZ() { return lookZ; }
    public void setLookZ(double v) { this.lookZ = v; }
}
