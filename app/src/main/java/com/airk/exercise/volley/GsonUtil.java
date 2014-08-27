package com.airk.exercise.volley;

/**
 * Created by kevin on 14-8-26.
 */
public class GsonUtil {
    public static class Welcome {
        public String _id;
        public String _rev;
        public String message;
    }

    public static class Item {
        public String _id;
        public String _rev;

        public String name;
        public String phone;
        public String address;
    }

    public static class InsertResponse {
        public String id;
        public String rev;
        public boolean ok;
    }

    public static class UpdateResponse {
        public String id;
        public String rev;
        public boolean ok;
    }
}
