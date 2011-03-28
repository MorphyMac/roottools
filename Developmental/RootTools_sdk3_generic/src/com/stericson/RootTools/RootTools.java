package com.stericson.RootTools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class RootTools {

    /*
     *This class is the gateway to every functionality within the RootTools library.
     *The developer should only have access to this class and this class only.
     *This means that this class should be the only one to be public.
     *The rest of the classes within this library must not have the public modifier.
     *
     *All methods and Variables that the developer may need to have access to should be here.
     *
     *If a method, or a specific functionality, requires a fair amount of code, or work to be done,
     *then that functionality should probably be moved to its own class and the call to it done here.
     *For examples of this being done, look at the remount functionality.
     */
	
    //--------------------
    //# Public Variables #
    //--------------------

	
    //------------------
    //# Public Methods #
    //------------------

    /**
     * This will launch the Android market looking for BusyBox
     * 
     * @param activity  pass in your Activity
     */
    public static void offerBusyBox(Activity activity) {
        Log.i(InternalVariables.TAG, "Launching Market for BusyBox");
        Intent i = new Intent(
                Intent.ACTION_VIEW, Uri.parse("market://details?id=stericson.busybox"));
        activity.startActivity(i);
    }

    /**
     * This will launch the Android market looking for BusyBox,
     * but will return the intent fired and starts the activity with startActivityForResult
     * 
     * @param activity      pass in your Activity
     * 
     * @param requestCode   pass in the request code
     * 
     * @return              intent fired
     */
    public static Intent offerBusyBox(Activity activity, int requestCode) {
        Log.i(InternalVariables.TAG, "Launching Market for BusyBox");
        Intent i = new Intent(
                Intent.ACTION_VIEW, Uri.parse("market://details?id=stericson.busybox"));
        activity.startActivityForResult(i, requestCode);
        return i;
    }

    /**
     * This will launch the Android market looking for SuperUser
     * 
     * @param activity  pass in your Activity
     */
    public static void offerSuperUser(Activity activity) {
        Log.i(InternalVariables.TAG, "Launching Market for SuperUser");
        Intent i = new Intent(
                Intent.ACTION_VIEW, Uri.parse("market://details?id=com.noshufou.android.su"));
        activity.startActivity(i);
    }

    /**
     * This will launch the Android market looking for SuperUser,
     * but will return the intent fired and starts the activity with startActivityForResult
     * 
     * @param activity      pass in your Activity
     * 
     * @param requestCode   pass in the request code
     * 
     * @return              intent fired
     */
    public static Intent offerSuperUser(Activity activity, int requestCode) {
        Log.i(InternalVariables.TAG, "Launching Market for SuperUser");
        Intent i = new Intent(
                Intent.ACTION_VIEW, Uri.parse("market://details?id=com.noshufou.android.su"));
        activity.startActivityForResult(i, requestCode);
        return i;
    }

    /**
     * 
     * @return  <code>true</code> if su was found
     * 
     * @deprecated As of release 0.7, replaced by {@link #isRootAvailable()}
     */
    @Deprecated
    public static boolean rootAvailable() {
        return isRootAvailable();
    }

    /**
     * @return  <code>true</code> if su was found.
     */
    public static boolean isRootAvailable() {
        Log.i(InternalVariables.TAG, "Checking for Root binary");
        String[] places = { "/system/bin/", "/system/xbin/",
                "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/" };
        for (String where : places) {
            File file = new File(where + "su");
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return <code>true</code> if BusyBox was found
     * 
     * @deprecated As of release 0.7, replaced by {@link #isBusyboxAvailable()}
     */
    @Deprecated
    public static boolean busyboxAvailable() {
        return isBusyboxAvailable();
    }

    /**
     * @return  <code>true</code> if BusyBox was found.
     */
    public static boolean isBusyboxAvailable() {
        Log.i(InternalVariables.TAG, "Checking for BusyBox");
        File tmpDir = new File("/data/local/tmp");
        if (!tmpDir.exists()) {
            InternalMethods.instance().doExec(new String[]{"mkdir /data/local/tmp"});
        }
        Set<String> tmpSet = new HashSet<String>();
        //Try to read from the file.
        LineNumberReader lnr = null;
        try {
            InternalMethods.instance().doExec(new String[]{"dd if=/init.rc of=/data/local/tmp/init.rc",
                    "chmod 0777 /data/local/tmp/init.rc"});
            lnr = new LineNumberReader( new FileReader( "/data/local/tmp/init.rc" ) );
            String line;
            while( (line = lnr.readLine()) != null ){
                if (line.contains("export PATH")) {
                    int tmp = line.indexOf("/");
                    tmpSet = new HashSet<String>(Arrays.asList(line.substring(tmp).split(":")));
                    for(String paths : tmpSet) {
                        File file = new File(paths + "/busybox");
                        if (file.exists()) {
                            Log.i(InternalVariables.TAG, "Found BusyBox!");
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.i(InternalVariables.TAG, "BusyBox was not found, some error happened!");
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * @return  <code>true</code> if your app has been given root access.
     * 
     * @deprecated As of release 0.7, replaced by {@link #isAccessGiven()} 
     */
    @Deprecated
    public static boolean accessGiven() {
        return isAccessGiven();
    }

    /**
     * @return  <code>true</code> if your app has been given root access.
     */
    public static boolean isAccessGiven() {
        Log.i(InternalVariables.TAG, "Checking for Root access");
        InternalVariables.accessGiven = false;
        InternalMethods.instance().doExec(new String[]{"id"});

        if (InternalVariables.accessGiven) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if there is enough Space on SDCard
     * 
     * @param updateSize    size to Check (long)
     *
     * @return              <code>true</code> if the Update will fit on SDCard,
     *                      <code>false</code> if not enough space on SDCard.
     *                      Will also return <code>false</code>,
     *                      if the SDCard is not mounted as read/write
     * 
     * @deprecated As of release 0.7, replaced by {@link #hasEnoughSpaceOnSdCard(long)}
     */
    @Deprecated
    public static boolean EnoughSpaceOnSdCard(long updateSize) {
        return hasEnoughSpaceOnSdCard(updateSize);
    }

    /**
     * Checks if there is enough Space on SDCard
     * 
     * @param updateSize    size to Check (long)
     *
     * @return              <code>true</code> if the Update will fit on SDCard,
     *                      <code>false</code> if not enough space on SDCard.
     *		                Will also return <code>false</code>,
     *		                if the SDCard is not mounted as read/write
     */
    public static boolean hasEnoughSpaceOnSdCard(long updateSize) {
        Log.i(InternalVariables.TAG, "Checking SDcard size and that it is mounted as RW");
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return (updateSize < availableBlocks * blockSize);
    }

    //TODO ALL - I would like to see the SendShell commands moved to their own class.
    //That class should be package-private and this class should pass requests to that class.
    //If you want to see an example of this, look at Remounter.
    //I did not move these yet because of the ties to Chris's work.
    //TODO
    
    /**
     * Sends one shell command as su (attempts to)
     * 
     * @param command   command to send to the shell
     *
     * @param result    injected result object that implements the Result class
     *
     * @return          a <code>LinkedList</code> containing each line that was returned
     *                  by the shell after executing or while trying to execute the given commands.
     *                  You must iterate over this list, it does not allow random access,
     *                  so no specifying an index of an item you want,
     *                  not like you're going to know that anyways.
     *
     * @throws InterruptedException
     *
     * @throws IOException
     *
     * @throws RootToolsException
     */
    public static List<String> sendShell(String command, Result result)
            throws IOException, InterruptedException, RootToolsException {
        return sendShell(new String[] { command}, 0, result);
    }

    /**
     * Sends one shell command as su (attempts to)
     *
     * @param command   command to send to the shell
     *
     * @return          a LinkedList containing each line that was returned by the shell
     *                  after executing or while trying to execute the given commands.
     *                  You must iterate over this list, it does not allow random access,
     *                  so no specifying an index of an item you want,
     *                  not like you're going to know that anyways.
     *
     * @throws InterruptedException
     *
     * @throws IOException
     */
    public static List<String> sendShell(String command)
            throws IOException, InterruptedException, RootToolsException {
        return sendShell(command, null);
    }

    /**
     * Sends several shell command as su (attempts to)
     * 
     * @param commands  array of commands to send to the shell
     *
     * @param sleepTime time to sleep between each command, delay.
     *
     * @param result    injected result object that implements the Result class
     *
     * @return          a <code>LinkedList</code> containing each line that was returned
     *                  by the shell after executing or while trying to execute the given commands.
     *                  You must iterate over this list, it does not allow random access,
     *                  so no specifying an index of an item you want,
     *                  not like you're going to know that anyways.
     *
     * @throws InterruptedException
     *
     * @throws IOException
     */
    public static List<String> sendShell(String[] commands, int sleepTime, Result result)
            throws IOException, InterruptedException, RootToolsException {
        Log.i(InternalVariables.TAG, "Sending " + commands.length + " shell command" + (commands.length>1?"s":""));
        List<String> response = null;
        if(null == result) {
            response = new LinkedList<String>();
        }

        Process process = null;
        DataOutputStream os = null;
        InputStreamReader osRes = null;

        try {
            process = Runtime.getRuntime().exec("su");
            if(null != result) {
                result.setProcess(process);
            }
            os = new DataOutputStream(process.getOutputStream());
            osRes = new InputStreamReader(process.getInputStream());
            BufferedReader reader = new BufferedReader(osRes);
            // Doing Stuff ;)
            for (String single : commands) {
                os.writeBytes(single + "\n");
                os.flush();
                Thread.sleep(sleepTime);
            }

            os.writeBytes("exit \n");
            os.flush();

            String line = reader.readLine();

            while (line != null) {
                if(null == result) {
                    response.add(line);
                } else {
                    result.process(line);
                }
                line = reader.readLine();
            }
        }
        catch (Exception ex) {
            if(null != result) {
                result.onFailure(ex);
            }
        }
        finally {
            int diag = process.waitFor();
            if(null != result) {
                result.onComplete(diag);
            }

            try {
                if (os != null) {
                    os.close();
                }
                if (osRes != null) {
                    osRes.close();
                }
                process.destroy();
            } catch (Exception e) {
                //return what we have
                return response;
            }
            return response;
        }
    }

    /**
     * Sends several shell command as su (attempts to)
     *
     * @param commands  array of commands to send to the shell
     *
     * @param sleepTime time to sleep between each command, delay.
     *
     * @return          a LinkedList containing each line that was returned by the shell
     *                  after executing or while trying to execute the given commands.
     *                  You must iterate over this list, it does not allow random access,
     *                  so no specifying an index of an item you want,
     *                  not like you're going to know that anyways.
     *
     * @throws InterruptedException
     *
     * @throws IOException
     */
    public static List<String> sendShell(String[] commands, int sleepTime)
            throws IOException, InterruptedException, RootToolsException {
        return sendShell(commands, sleepTime,  null);
    }

    /**
     * This will take a path, which can contain the file name as well,
     * and attempt to remount the underlying partition.
     * 
     * For example, passing in the following string:
     * "/system/bin/some/directory/that/really/would/never/exist"
     * will result in /system ultimately being remounted.
     * However, keep in mind that the longer the path you supply, the more work this has to do,
     * and the slower it will run.
     * 
     * @param file      file path
     * 
     * @param mountType mount type: pass in RO (Read only) or RW (Read Write)
     * 
     * @return          a <code>boolean</code> which indicates whether or not the partition
     *                  has been remounted as specified.
     */

    public static boolean remount(String file, String mountType) {
        //Recieved a request, get an instance of Remounter
    	Remounter remounter = new Remounter();
    	//send the request.
    	return(remounter.remount(file, mountType));
    }

    /**
     * This method can be used to unpack a binary from the raw resources folder and store it in
     * /data/data/app.package/files/
     * This is typically useful if you provide your own C- or C++-based binary.
     * This binary can then be executed using sendShell() and its full path.
     *
     * @param context   the current activity's <code>Context</code>
     *
     * @param sourceId  resource id; typically <code>R.raw.id</code>
     *
     * @param destName  destination file name; appended to /data/data/app.package/files/
     *
     * @param mode      chmod value for this file
     *
     * @return          a <code>boolean</code> which indicates whether or not we were
     *                  able to create the new file.
     */
    public static boolean installBinary(Context context, int sourceId, String destName, String mode) {
        Installer installer;

        try {
            installer = new Installer(context);
        }
        catch(IOException ex) {
            return false;
        }

        if(installer.installBinary(sourceId, destName, mode)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * This method can be used to unpack a binary from the raw resources folder and store it in
     * /data/data/app.package/files/
     * This is typically useful if you provide your own C- or C++-based binary.
     * This binary can then be executed using sendShell() and its full path.
     *
     * @param context   the current activity's <code>Context</code>
     *
     * @param sourceId  resource id; typically <code>R.raw.id</code>
     *
     * @param destName  destination file name; appended to /data/data/app.package/files/
     *
     * @return          a <code>boolean</code> which indicates whether or not we were
     *                  able to create the new file.
     */
    public static boolean installBinary(Context context, int sourceId, String destName) {
        return installBinary(context, sourceId, destName, "700");
    }

    //--------------------
    //# Internal methods #
    //--------------------
    // These should be moved to Internal if possible.
    //TODO Chris - I will let you move these as you see fit.
    
    public static abstract class Result {
        private Process         process = null;
        private Serializable    data    = null;
        private int             error   = 0;

        public abstract void process(String line) throws Exception;
        public abstract void onFailure(Exception ex);
        public abstract void onComplete(int diag);

        protected Result    setProcess(Process process) { this.process = process; return this; }
        public Process      getProcess()                { return process; }
        protected Result    setData(Serializable data)  { this.data = data; return this; }
        public Serializable getData()                   { return data; }
        protected Result    setError(int error)         { this.error = error; return this; }
        public int          getError()                  { return error; }
    }

    public static class RootToolsException extends Exception {
        public RootToolsException(Throwable th) {
            super(th);
        }
    }
}