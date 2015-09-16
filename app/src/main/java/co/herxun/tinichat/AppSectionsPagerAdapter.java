package co.herxun.tinichat;

import co.herxun.tinichat.fragment.FriendsListFragment;

import co.herxun.tinichat.fragment.TopicListFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Aadapter for ViewPager
 */
public class AppSectionsPagerAdapter extends FragmentPagerAdapter {
	FriendsListFragment mFrag_friend;
	TopicListFragment mFragme_topic;
	
	public AppSectionsPagerAdapter(FragmentManager fm) {
		super(fm);
		mFrag_friend = new FriendsListFragment();
		mFragme_topic =  new TopicListFragment();
	}

	@Override
	public Fragment getItem(int i) {
		switch (i) {
		case Utils.Constant.Fragment.ALL_USERS:
			return mFrag_friend;
		case Utils.Constant.Fragment.TOPICS:
			return mFragme_topic;
		default:
			return mFrag_friend;
		}
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public CharSequence getPageTitle(int i) {
		switch (i) {
		case 0:
			return "All Users";
		case 1:
			return "Topics";
		default:
			return "?";
		}
	}
}
