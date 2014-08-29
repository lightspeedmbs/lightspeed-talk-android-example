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
			
		}
		public class HistoryItem{
			public static final String FROM = "from";
			public static final String MSG = "msg";
			public static final String DATA = "data";
			public static final String CONTENT = "content";
			public static final String TYPE = "type";
			
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
