package com.groupdevotions.server.service;

import com.groupdevotions.shared.model.GroupMemberActivity;

import java.util.ArrayList;
import java.util.List;

public class BlogData {
    public String groupName;
    public String blogInstructions;
    public List<GroupMemberActivity> groupMemberActivities = new ArrayList<GroupMemberActivity>();
}
