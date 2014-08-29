package co.herxun.tinichat;

public class Utils {
	public class Constant{
		public class RoomType{
			public static final int DEFAULT = -1;
			public static final int CLIENT = 0;
			public static final int TOPIC = 1;
		}
		public class AttachmentType{
			public static final String IMAGE = "image";
			public static final String LINK = "link";
			public static final String VIDEO = "video";
			public static final String LOCATION = "location";	
			public static final String TEXT = "text";	
			public static final String NULL = "null";	
		}
		public class MsgCustomData{
			public static final String DATA = "data";
			public static final String TYPE = "type";
		}
		public class Fragment{
			public static final int ALL_USERS = 0;
			public static final int TOPICS = 1;
		}
	}
}
