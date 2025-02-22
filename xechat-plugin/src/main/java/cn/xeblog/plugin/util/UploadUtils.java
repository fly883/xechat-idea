package cn.xeblog.plugin.util;

import cn.hutool.core.thread.GlobalThreadPool;
import cn.hutool.core.util.IdUtil;
import cn.xeblog.commons.entity.UserMsgDTO;
import cn.xeblog.commons.enums.Action;
import cn.xeblog.plugin.action.ConsoleAction;
import cn.xeblog.plugin.action.MessageAction;
import com.intellij.util.ui.ImageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author anlingyi
 * @date 2020/9/10
 */
public class UploadUtils {

    private static boolean UPLOADING;
    private static final String ACCEPT_IMAGE_TYPE = "jpg,jpeg,gif,png";
    private static final int MAX_SIZE = 10 << 20;

    public static void uploadImageFile(File file) {
        String fileType = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        if (ACCEPT_IMAGE_TYPE.indexOf(fileType.toLowerCase()) < 0) {
            ConsoleAction.showSimpleMsg("不支持的图片类型！");
            return;
        }

        try (FileInputStream inputStream = new FileInputStream(file)) {
            sendImgAsync(IOUtils.toByteArray(inputStream), generateFileName(fileType));
        } catch (Exception e) {
            e.printStackTrace();
            ConsoleAction.showSimpleMsg("图片上传失败！");
        }
    }

    public static void uploadImage(Image image) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BufferedImage bufferedImage = ImageUtil.toBufferedImage(image);
            ImageIO.write(bufferedImage, "png", out);
            sendImgAsync(out.toByteArray(), generateFileName("png"));
        } catch (IOException e) {
            e.printStackTrace();
            ConsoleAction.showSimpleMsg("图片上传失败！");
        }
    }

    private static String generateFileName(String fileType) {
        fileType = fileType == null ? "jpg" : fileType;
        return IdUtil.fastUUID() + "." + fileType;
    }

    private static void sendImgAsync(byte[] bytes, String fileName) {
        if (UPLOADING) {
            ConsoleAction.showSimpleMsg("请等待之前的图片上传完成！");
            return;
        }

        if (bytes.length > MAX_SIZE) {
            ConsoleAction.showSimpleMsg("图片大小不能超过" + (MAX_SIZE >> 20) + "MB！");
            return;
        }

        UPLOADING = true;
        ConsoleAction.showSimpleMsg("图片上传中...");

        GlobalThreadPool.execute(() -> {
            try {
                ByteBuf byteBuf = Unpooled.buffer();
                byteBuf.writeInt(fileName.length());
                byteBuf.writeBytes(fileName.getBytes());
                byteBuf.writeBytes(bytes);
                MessageAction.send(new UserMsgDTO(byteBuf.array(), UserMsgDTO.MsgType.IMAGE), Action.CHAT);
            } catch (Exception e) {
                ConsoleAction.showSimpleMsg("图片上传失败！");
                e.printStackTrace();
            } finally {
                UPLOADING = false;
            }
        });
    }
}
