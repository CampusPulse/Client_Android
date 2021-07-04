package com.redtop.engaze;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.adapter.ContactOrGroupListAdapter;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.ContactOrGroup;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ContactsListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
	private SearchView mSearchView;
	private ListView mListView;    
	public Handler mHandler;
	private ContactOrGroupListAdapter mAdapter;

	private ViewGroup mFlowContainer;
	private RelativeLayout mInviteeSection;
	private ImageButton mAddInvitees;
	public static Boolean isFirstTime = false;

	//ArrayList<ContactOrGroup> mMembers = new ArrayList<ContactOrGroup> ();
	ArrayList<ContactOrGroup>mAddedMembers;
	ArrayList<Integer>contactGroupPositions = new ArrayList<Integer>();
	private LinearLayout mLl_nocontacts;
	private SwipeRefreshLayout swipeRefreshLayout;
	private ArrayList<ContactOrGroup> mAllMembers;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		mContext = this;				
		setContentView(R.layout.activity_contact_group);
		Toolbar toolbar = (Toolbar) findViewById(R.id.search_contact_toolbar);
		String caller  = getIntent().getStringExtra("caller");
		if(caller!=null && caller.equals(HomeActivity.class.toString())){
			PreffManager.setPrefArrayList("Invitees", null);
		}
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
			getSupportActionBar().setTitle(R.string.title_select_friends);							
			toolbar.setNavigationOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});
		}
		initializeElements();
		if(!accessingContactsFirstTime()){
			//mMembers = ContactAndGroupListManager.getAllRegisteredContacts(mContext);
			mAllMembers = ContactAndGroupListManager.getAllContacts();
			loadFriendList();
		}

		mAddedMembers = PreffManager.getPrefArrayList("Invitees");
		if(mAddedMembers!=null){
			mInviteeSection.setVisibility(View.VISIBLE);
			mAddInvitees.setVisibility(View.VISIBLE);
			for(ContactOrGroup cg : mAddedMembers){
				createContactLayoutItem(cg);
			}
		}
		else{
			mInviteeSection.setVisibility(View.GONE);
			mAddInvitees.setVisibility(View.GONE);
			mAddedMembers = new ArrayList<ContactOrGroup>();			
		}

		initializeClickEvents();

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
		swipeRefreshLayout.setOnRefreshListener(this);
	}
	/**
	 * This method is called when swipe refresh is pulled down
	 */
	@Override
	public void onRefresh() {
		swipeRefreshLayout.setRefreshing(true);
		ContactAndGroupListManager.refreshMemberList();
		//swipeRefreshLayout.setRefreshing(false);
	}

	@Override
	protected void registeredMemberListCached(){
		//mMembers = ContactAndGroupListManager.getAllRegisteredContacts(mContext);
				mAllMembers = ContactAndGroupListManager.getAllContacts();
				loadFriendList();
	}
	
	@Override
	protected void memberListRefreshed_success(HashMap<String, ContactOrGroup> memberList){
		if(memberList!=null && memberList.values().size()>0){

			//mMembers = ContactAndGroupListManager.sortContacts(new ArrayList<ContactOrGroup>(memberList.values()));
			loadFriendList();
		}	

		swipeRefreshLayout.setRefreshing(false);
	}

	@Override
	protected void memberListRefreshed_fail(){
		swipeRefreshLayout.setRefreshing(false);
	}

	private void loadFriendList(){										

		if(mAllMembers==null || mAllMembers.size()== 0)
		{
			mAllMembers = new ArrayList<ContactOrGroup>();
			inviteFriend();			
			mLl_nocontacts.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);

		}else{
			mLl_nocontacts.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
			mAdapter = new ContactOrGroupListAdapter(mContext,R.layout.item_contact_group_list, mAllMembers);
			mListView.setAdapter(mAdapter);
			mListView.setTextFilterEnabled(true);
		}
	}

	private void initializeClickEvents() {

		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position,
					long arg3) 
			{
				ContactOrGroup value = (ContactOrGroup)adapter.getItemAtPosition(position);	
				//v.setSelected(true); 

				if(mAddedMembers.size() < 10) {

					if(mAddedMembers.size()==0){
						//removeHintText();
						mAddInvitees.setVisibility(View.VISIBLE);
						mInviteeSection.setVisibility(View.VISIBLE);
					}

					Boolean alreadyAdded = false;
					for (ContactOrGroup cg  : mAddedMembers ){
						if(cg.userId!=null && cg.userId.equals(value.userId)){
							alreadyAdded = true;
							break;
						}
					}
					if(!alreadyAdded){
						mAddedMembers.add(value);
						createContactLayoutItem(value);
						contactGroupPositions.add(position);
					}
					else
					{
						Toast.makeText(mContext,
								"User is already added", Toast.LENGTH_SHORT).show();
					}


					mSearchView.setQuery("", false);
					mSearchView.clearFocus();
					mListView.clearTextFilter();
					mAdapter.getFilter().filter("");
				}
				else{
					Toast.makeText(mContext,
							"You have reached maximum limit of participants!", Toast.LENGTH_SHORT).show();
				}
			}
		});


		mAddInvitees.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PreffManager.setPrefArrayList("Invitees", mAddedMembers);
				Intent intent = new Intent();

				setResult(RESULT_OK, intent);        
				finish();					

			}
		});
	}


	private void initializeElements() {

		mListView = (ListView) findViewById(R.id.list_contact_group_view);
		mAddInvitees = (ImageButton)findViewById(R.id.img_add_invitees);
		mFlowContainer = (ViewGroup) findViewById(R.id.participant_layout);
		mInviteeSection = (RelativeLayout)findViewById(R.id.invitee_section);
		mAddInvitees = (ImageButton)findViewById(R.id.img_add_invitees);						
		mLl_nocontacts = (LinearLayout)findViewById(R.id.ll_contacts_help_text);
	}

	private void createContactLayoutItem(ContactOrGroup cg){		
		int childrenCount= mFlowContainer.getChildCount();
		LinearLayout contactLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.contact_item_layout_template, null);

		TextView lblname = (TextView)contactLayout.getChildAt(0);
		lblname.setText(cg.getName());
		lblname.setTag(cg);

		contactLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {			
				mFlowContainer.removeView(v);				
				if(mFlowContainer.getChildCount()==0){
					mInviteeSection.setVisibility(View.GONE);
					mAddInvitees.setVisibility(View.GONE);
				}				
				mAddedMembers.remove((ContactOrGroup)((LinearLayout)v).getChildAt(0).getTag()); 
			}
		});		

		mFlowContainer.addView(contactLayout, childrenCount-1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {	
		getMenuInflater().inflate(R.menu.menu_search, menu);
	
		MenuItem searchItem = menu.findItem(R.id.menu_search);
		mSearchView= (SearchView) MenuItemCompat.getActionView(searchItem);
		//*** setOnQueryTextFocusChangeListener ***
		mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

			}
		});

		mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {

				return false;
			}

			@Override
			public boolean onQueryTextChange(String searchQuery) {
				mAdapter.filter(searchQuery.toString().trim());
				mListView.invalidate();
				return true;
			}
		});

		MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				// Do something when collapsed
				return true;  // Return true to collapse action view
			}

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				// Do something when expanded
				return true;  // Return true to expand action view
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){	        	
		case R.id.action_add_member_contactlist:
			inviteFriend();
			break;			
		}       
		return super.onOptionsItemSelected(item);
	}
}
