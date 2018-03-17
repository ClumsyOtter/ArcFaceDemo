package com.cr.GraduateDesign;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.guo.android_extend.java.ExtInputStream;
import com.guo.android_extend.java.ExtOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FaceDB {
    private final String TAG = this.getClass().toString();

    static String appid = "8xXmr9TLkja3Xk2r6HX36YoCHLXgBWoVTJ4VFaWtW2bi";
    static String ft_key = "FcCHyk8FHDAwmYzTdo8gmvnScTjfJtPtPpeght8GSpRf";
    static String fd_key = "FcCHyk8FHDAwmYzTdo8gmvnZmrzsMFybyg8PNnUoUsBr";
    static String fr_key = "FcCHyk8FHDAwmYzTdo8gmvngwGFzdo2UbNe9um4JSMta";
    static String age_key = "FcCHyk8FHDAwmYzTdo8gmvoJkGZpYDHkp2hzfUc3wCRQ";
    static String gender_key = "FcCHyk8FHDAwmYzTdo8gmvoRufq4S7REJU3RwWEGeNEK";
    private final SqliteFace sqliteFace;

    private String mDBPath;
    List<FaceRegist> mRegister;
    private AFR_FSDKEngine mFREngine;
    private AFR_FSDKVersion mFRVersion;
    private boolean mUpgrade;

    class FaceRegist {
        String mName;
        List<AFR_FSDKFace> mFaceList;

        FaceRegist(String name) {
            mName = name;
            mFaceList = new ArrayList<>();
        }
    }

    FaceDB(String path, Context context) {
        mDBPath = path;
        sqliteFace = new SqliteFace(context);
        mRegister = new ArrayList<>();
        mFRVersion = new AFR_FSDKVersion();
        mUpgrade = false;
        mFREngine = new AFR_FSDKEngine();
        AFR_FSDKError error = mFREngine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
        if (error.getCode() != AFR_FSDKError.MOK) {
            Log.e(TAG, "AFR_FSDK_InitialEngine fail! error code :" + error.getCode());
        } else {
            mFREngine.AFR_FSDK_GetVersion(mFRVersion);
            Log.d(TAG, "AFR_FSDK_GetVersion=" + mFRVersion.toString());
        }
    }

    public void destroy() {
        if (mFREngine != null) {
            mFREngine.AFR_FSDK_UninitialEngine();
        }
    }

    private boolean saveInfo() {
        try {
            FileOutputStream fs = new FileOutputStream(mDBPath + "/face.txt");
            ExtOutputStream bos = new ExtOutputStream(fs);
            bos.writeString(mFRVersion.toString() + "," + mFRVersion.getFeatureLevel());
            bos.close();
            fs.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean loadInfo() {
        if (!mRegister.isEmpty()) {
            return false;
        }
        try {
            FileInputStream fs = new FileInputStream(mDBPath + "/face.txt");
            ExtInputStream bos = new ExtInputStream(fs);
            //load version
            String version_saved = bos.readString();
            if (version_saved.equals(mFRVersion.toString() + "," + mFRVersion.getFeatureLevel())) {
                mUpgrade = true;
            }
            //load all regist name.
            for (String name = bos.readString(); name != null; name = bos.readString()) {
                if (new File(mDBPath + "/" + name + ".data").exists()) {
                    mRegister.add(new FaceRegist(name));
                }
            }
            bos.close();
            fs.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    void loadFaces() {
        // if (loadInfo()) {

//                for (FaceRegist face : mRegister) {
//                    Log.d(TAG, "load name:" + face.mName + "'s face feature data.");
//                    FileInputStream fs = new FileInputStream(mDBPath + "/" + face.mName + ".data");
//                    ExtInputStream bos = new ExtInputStream(fs);
//                    AFR_FSDKFace afr = null;
//                    do {
//                        if (afr != null) {
//                            if (mUpgrade) {
//                            }
//                            face.mFaceList.add(afr);
//                        }
//                        afr = new AFR_FSDKFace();
//                    } while (bos.readBytes(afr.getFeatureData()));
//                    bos.close();
//                    fs.close();
//                    Log.d(TAG, "load name: size = " + face.mFaceList.size());

        Cursor faceCursor = sqliteFace.findAllFace();

        //判断是否有数据
        if (faceCursor != null && faceCursor.moveToFirst()) {
            Log.i("存储数量：", "   " + faceCursor.getCount());
            while (faceCursor.moveToNext()) {
                String name = faceCursor.getString(faceCursor.getColumnIndex("name"));
                String face = faceCursor.getString(faceCursor.getColumnIndex("face"));
                byte[] bytes = face.getBytes();
                AFR_FSDKFace afr_FSDKFace = new AFR_FSDKFace(bytes);
                FaceRegist regist = new FaceRegist(name);
                regist.mFaceList.add(afr_FSDKFace);
                mRegister.add(regist);
            }
        }

        Log.i("mRegister", mRegister.size() + " ");
        // }
    }

    void addFace(String name, AFR_FSDKFace face) {
        //加入注册容器
        boolean add = true;
        for (FaceRegist frface : mRegister) {
            if (frface.mName.equals(name)) {
                frface.mFaceList.add(face);
                add = false;
                break;
            }
        }
        if (add) { // not registered.
            FaceRegist frface = new FaceRegist(name);
            frface.mFaceList.add(face);
            mRegister.add(frface);
        }

        //存入数据库
        sqliteFace.insert(name, new String(face.getFeatureData()));
    }

//            if (saveInfo()) {
//                //update all names
//                FileOutputStream fs = new FileOutputStream(mDBPath + "/face.txt", true);
//                ExtOutputStream bos = new ExtOutputStream(fs);
//                for (FaceRegist frface : mRegister) {
//                    bos.writeString(frface.mName);
//                }
//                bos.close();
//                fs.close();

    //save new feature
//                fs = new FileOutputStream(mDBPath + "/" + name + ".data", true);
//                bos = new ExtOutputStream(fs);
//                bos.writeBytes(face.getFeatureData());
//                bos.close();
//                fs.close();

//    } catch(
//    IOException e)
//
//    {
//        e.printStackTrace();
//    }

    // }

    void delete(String name) {

        boolean find = false;
        for (FaceRegist frface : mRegister) {
            if (frface.mName.equals(name)) {
                mRegister.remove(frface);
                find = true;
                break;
            }
            sqliteFace.delete(name);
        }
    }
//            if (find) {
//                if (saveInfo()) {
//                    //update all names
//                    FileOutputStream fs = new FileOutputStream(mDBPath + "/face.txt", true);
//                    ExtOutputStream bos = new ExtOutputStream(fs);
//                    for (FaceRegist frface : mRegister) {
//                        bos.writeString(frface.mName);
//                    }
//                    bos.close();
//                    fs.close();
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    //}

    public boolean upgrade() {
        return false;
    }
}
