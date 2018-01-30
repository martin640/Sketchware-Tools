package io.ready.tools;

import android.os.FileObserver;

public class IdHelper {

    public static int getId(String path) {
        StringBuilder builder = new StringBuilder();

        char[] charArray = path.toCharArray();

        for (char c : charArray) {

            if (isNumeric(c) && builder.length() < 3) {
                builder.append(c);
            } else {
                if (builder.length() != 3)
                    builder = new StringBuilder();
            }
        }

        if (builder.length() == 3) {
            return Integer.valueOf(builder.toString());
        } else {
            return -1;
        }
    }

    public static boolean isNumeric(char u) {
        String s = String.valueOf(u);
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    public static class FileWatcher extends RecursiveFileObserver {
        public String absolutePath;
        private EventListener listener;

        public FileWatcher(String path) {
            super(path, FileObserver.ALL_EVENTS);
            absolutePath = path;
            listener = new EventListener() {
                @Override
                public void onCreateEvent(String path) {

                }

                @Override
                public void onModifyEvent(String path) {

                }

                @Override
                public void onDeleteEvent(String path) {

                }

                @Override
                public void onUpdateEvent(String path) {

                }

                @Override
                public void onAccessEvent(String path) {

                }
            };
        }

        public interface EventListener {
            public void onCreateEvent(String path);
            public void onModifyEvent(String path);
            public void onDeleteEvent(String path);
            public void onUpdateEvent(String path);
            public void onAccessEvent(String path);
        }

        public void setEventListener(EventListener listener) {
            this.listener = listener;
        }

        @Override
        public void onEvent(int event, String path) {
            if (path == null) {
                return;
            }
            //a new file or subdirectory was created under the monitored directory
            if ((FileObserver.CREATE & event)!=0) {
                FileAccessLogStatic.accessLogMsg += absolutePath + "/" + path + " is createdn";
                listener.onCreateEvent(path);
            }
            if ((FileObserver.ACCESS & event)!=0) {
                FileAccessLogStatic.accessLogMsg += absolutePath + "/" + path + " is accessed";
                listener.onAccessEvent(path);
            }
            //data was written to a file
            if ((FileObserver.MODIFY & event)!=0) {
                FileAccessLogStatic.accessLogMsg += absolutePath + "/" + path + " is modifiedn";
                listener.onModifyEvent(path);
            }
            //a file was deleted from the monitored directory
            if ((FileObserver.DELETE & event)!=0) {
                //for testing copy file
    // FileUtils.copyFile(absolutePath + "/" + path);
                FileAccessLogStatic.accessLogMsg += absolutePath + "/" + path + " is deletedn";
                listener.onDeleteEvent(path);
            }
            //the monitored file or directory was deleted, monitoring effectively stops
            if ((FileObserver.DELETE_SELF & event)!=0) {
                FileAccessLogStatic.accessLogMsg += absolutePath + "/" + " is deletedn";
                listener.onDeleteEvent(path);
            }
            //Metadata (permissions, owner, timestamp) was changed explicitly
            if ((FileObserver.ATTRIB & event)!=0) {
                FileAccessLogStatic.accessLogMsg += absolutePath + "/" + path + " is changed (permissions, owner, timestamp)n";
                listener.onUpdateEvent(path);
            }
        }
    }
}
