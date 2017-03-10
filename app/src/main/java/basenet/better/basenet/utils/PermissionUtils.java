package basenet.better.basenet.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限检测类，用于android6 +
 * <p>
 * <pre>
 *      使用方法：
 *      1.需要检查权限的地方：
 *      PermissionUtils.checkOnePermission(this, Manifest.permission.CAMERA, getResources().getString(R.string.me_permission_request_camera_info), () -> {
            toUserIcon();
        });

        2.权限回调：
        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            PermissionUtils.requestResult(requestCode, permissions, grantResults, () -> {
                toUserIcon();
            }, null);
         }
 * </pre>
 * </p>
 * Created by zhaoyu on 2016/5/11.
 */
public final class PermissionUtils {

    public static final int REQUEST_CODE = 20;

    /**
     * 一个性访问多个权限
     *
     * @param obj
     * @param permissions     权限列表
     * @param permissionNames 权限名称列表 -- 自己取名的
     */
    public static void checkPermissions(final Object obj, final List<String> permissions, final List<String> permissionNames, final Runnable runnable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 长度必须一致
            if (null == permissions || null == permissionNames || (permissions.size() != permissionNames.size())) {
                return;
            }

            // 检测类型
            Activity activity = getActivity(obj);
            if (activity == null) {
                return;
            }


            // 需要解释的权限
            final List<String> permissionsNeeded = new ArrayList<>();
            final List<String> permissionsList = new ArrayList<>();

            // 一次申请多个权限
            for (int i = 0; i < permissions.size(); i++) {
                if (addPermission(obj, permissionsList, permissions.get(i))) {
                    permissionsNeeded.add(permissionNames.get(i));
                }
            }

            if (permissionsList.size() > 0) {
                String appName = getApplicationName(activity);
                // 需要向用户解释，为什么需要这个权限
                if (permissionsNeeded.size() > 0) {
                    String message = appName + " 需要以下权限： " + permissionsNeeded.get(0);
                    for (int i = 1; i < permissionsNeeded.size(); i++) {
                        message = message + ", " + permissionsNeeded.get(i);
                    }

                    if (!activity.isFinishing()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(message);
                        builder.setCancelable(false);
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                request(obj, permissionsList);
                            }
                        });
                        builder.create().show();
                    }
                } else {
                    // 无需解释，直接请求权限
                    request(obj, permissionsList);
                }
            } else {
                if (runnable != null) {
                    runnable.run();
                }
            }
        } else {
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    /**
     * 请求单个权限
     *
     * @param obj
     * @param permission 权限名字，如：Manifest.permission.CAMERA
     * @param explain    ,如果上次权限被拒绝过，显示给用户的解释信
     * @param runnable   执行的任务
     * @return
     */
    public static void checkOnePermission(final Object obj, final String permission, final String explain, final Runnable runnable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检测类型
            Activity activity = getActivity(obj);
            if (activity == null) {
                return;
            }

            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                final List<String> permissionsList = new ArrayList<>();
                permissionsList.add(permission);

                // 解释信息
                if (!TextUtils.isEmpty(explain) && !activity.shouldShowRequestPermissionRationale(permission)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(explain);
                    builder.setCancelable(false);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 如果用户 shouldShowRequestPermissionRationale 过，requestPermissions 将不执行了
                            request(obj, permissionsList);
                        }
                    });
                    builder.create().show();
                } else {
                    request(obj, permissionsList);
                }
            } else {
                if (runnable != null) {
                    runnable.run();
                }
            }
        } else {
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    /**
     * 权限请求结果
     * onRequestPermissionsResult 调用
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @param successRunnable 权限获取成功，执行的任务
     * @param failRunnable    权限获取失败，执行的任务
     */
    public static void requestResult(int requestCode, String[] permissions, int[] grantResults, final Runnable successRunnable, final Runnable failRunnable) {
        if (requestCode == REQUEST_CODE) {
            boolean isGranted = true;
            int size = permissions.length;
            for (int i = 0; i < size; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                }
            }
            if (isGranted && successRunnable != null) {
                successRunnable.run();     // 执行任务
            } else if (!isGranted && failRunnable != null) { //  权限被拒绝
                failRunnable.run();     // 执行任务
            }
        }
    }

    /**
     * 请求权限
     *
     * @param object
     * @param permissionsList
     */
    private static void request(final Object object, List<String> permissionsList) {
        if (object instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) object, permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE);
        } else if (object instanceof Fragment) {
            ((Fragment) object).requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE);
        }
    }

    /**
     * 添加要求权限列表
     *
     * @param obj
     * @param permissionsList
     * @param permission
     * @return true 表示需要解释，false不需要
     */
    private static boolean addPermission(final Object obj, List<String> permissionsList, String permission) {
        // 检测类型
        Activity activity = getActivity(obj);
        if (activity == null) {
            return true;
        }

        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option, 曾经被用户拒绝过(用户手动拒绝，或者在权限设置里面，随时关闭)
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取权限请求的acitivity
     *
     * @param obj
     * @return
     */
    private static Activity getActivity(final Object obj) {
        Activity activity = null;
        if (obj instanceof Activity) {
            activity = (Activity) obj;
        } else if (obj instanceof Fragment) {
            Fragment fragment = (Fragment) obj;
            activity = fragment.isAdded() ? fragment.getActivity() : null;
        } else {
            throw new IllegalArgumentException("不支持的参数类型：" + obj.getClass());
        }
        return activity;
    }


    /**
     * 获取应用名称
     *
     * @param context
     * @return
     */
    private static String getApplicationName(final Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        String appName = "";
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            appName = (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (Exception e) {
            appName = "";
        }
        return appName;
    }

}
