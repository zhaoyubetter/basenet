package basenet.better.basenet.mae.bundles.update;

/**
 * 服务端返回的更新信息，封装类
 * Created by liyu20 on 2018/4/11.
 */
public class UpdateInfo {
    private String fileUrl;
    private boolean isForce;
    private boolean isUpdate;
    private String updateTitle;
    private String updateInfo;
    private String localFileDir;

    public UpdateInfo(String fileUrl, boolean isForce, boolean isUpdate, String updateTitle, String updateInfo, String localFileDir) {
        this.fileUrl = fileUrl;
        this.isForce = isForce;
        this.isUpdate = isUpdate;
        this.updateTitle = updateTitle;
        this.updateInfo = updateInfo;
        this.localFileDir = localFileDir;
    }

    public void setlocalFileDir(String localFileDir) {
        this.localFileDir = localFileDir;
    }

    public String getLocalFileFullPath() {
        return localFileDir + fileUrl.substring(fileUrl.lastIndexOf("/"));
    }

    public void setUpdateTitle(String updateTitle) {
        this.updateTitle = updateTitle;
    }

    public void setUpdateInfo(String updateInfo) {
        this.updateInfo = updateInfo;
    }

    public String getUpdateTitle() {

        return updateTitle;
    }

    public String getUpdateInfo() {
        return updateInfo;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public boolean isUpdate() {

        return isUpdate;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public boolean isForce() {
        return isForce;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public void setForce(boolean force) {
        isForce = force;
    }
}
