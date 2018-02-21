package com.groupdevotions.server;

import com.groupdevotions.server.rest.*;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by DanV on 1/10/2015.
 */
public class MainJerseyApplication extends Application {
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(AccountResource.class);
        s.add(BibleResource.class);
        s.add(BlogResource.class);
        s.add(ConfigResource.class);
        s.add(DevotionResource.class);
        s.add(GroupMemberResource.class);
        s.add(GroupResource.class);
        s.add(JournalResource.class);
        s.add(LessonResource.class);
        s.add(OrganizationResource.class);
        s.add(StudyResource.class);
        s.add(GsonMessageBodyHandler.class);
        //s.add(AuthorizationResource.class);
        //s.add(MessageBodyWriterXML.class);
        //s.add(MessageBodyWriterJSON.class);
        return s;
    }
}
