package com.thermatk.java.tgemoji;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static com.thermatk.java.tgemoji.EmojiData.fixEmoji;

class DrawableInfo {
    public final byte page;
    public final short page2;
    public final int emojiIndex;

    public DrawableInfo(byte p, short p2, int index) {
        page = p;
        page2 = p2;
        emojiIndex = index;
    }
}

public class Main {
    public static final String basePath = "workfiles/";
    public static HashMap<String, DrawableInfo> rects = new HashMap<>();

    public static void main(String[] args) {
        doTheMap();
        System.out.print("Make img Noto");
        makeImgsGoog2();
        System.out.print("Done");
    }

    public static void makeImgsGoog2() {

        new File(basePath + "ready/imgsNoto125/").mkdirs();

        for (Map.Entry<String, DrawableInfo> entry: rects.entrySet()) {
            try {
                DrawableInfo drInfo = entry.getValue();
                String emojiKey = entry.getKey();
                String pathKey = "emoji_u" + emojiKey.replace("-","_");

                String path = basePath + "inputs/noto-emoji-sep25/72/" + pathKey + ".png";
                File f = new File(path);
                boolean exists = false;
                if(f.exists()) {
                    exists = true;
                } else {
                    // try quick fe0f fixes
                    String feofPath = path;
                    feofPath = feofPath.replace("_fe0f_", "_");
                    feofPath = feofPath.replace("_fe0f", "");
                    f = new File(feofPath);
                    if(f.exists()) {
                        exists = true;
                    } else {
                        // they append zeroes, fix
                        String[] cps = entry.getKey().split("-");
                        String newS = "";
                        for (int k=0;k<cps.length;k++) {
                            String cp = cps[k];
                            if (k>0) {
                                newS +="_";
                            }

                            if (cp.length() == 2) {
                                newS +="00" + cp;
                            } else {
                                newS += cp;
                            }
                        }
                        pathKey = "emoji_u" + newS;
                        path = basePath + "inputs/noto-emoji-sep25/72/" + pathKey + ".png";
                        f = new File(path);
                        if(f.exists()) {
                            exists = true;
                        } else {
                            // Twemoji fallback
                            path = basePath + "inputs/twemoji-master14/72x72/" + emojiKey + ".png";
                            f = new File(path);
                            if(f.exists()) {
                                exists = true;
                            }
                        }
                    }
                }

                if (exists) {
                    BufferedImage image72 = ImageIO.read(f);
                    BufferedImage image66 = resize(image72, 66,66);
                    ImageIO.write(image66, "PNG", new File(basePath+"ready/imgsNoto125/"+drInfo.page + "_" + drInfo.page2+".png"));
                } else {
                    System.out.println("(GOOGLE) ERROR MISSING: " +drInfo.page + "_" + drInfo.page2 + "::"+ emojiKey);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String toCodePoint(String emoji) {
        StringBuilder sb = new StringBuilder();
        int[] codepoints = emoji.codePoints().toArray();
        for (int i = 0; i < codepoints.length; i++) {
            if (i > 0) sb.append("-");
            sb.append(Integer.toHexString(codepoints[i]));
        }
        return sb.toString();
    }

    // code initally from https://github.com/DrKLO/Telegram/blob/master/TMessagesProj/src/main/java/org/telegram/messenger/Emoji.java
    public static void doTheMap() {
        for (int j = 0; j < EmojiData.data.length; j++) {
            for (int i = 0; i < EmojiData.data[j].length; i++) {
                String name = toCodePoint(fixEmoji(EmojiData.data[j][i]));
                rects.put(name, new DrawableInfo((byte) j, (short) i, i));
            }
        }
    }

    // from http://stackoverflow.com/a/9417836
    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
}
