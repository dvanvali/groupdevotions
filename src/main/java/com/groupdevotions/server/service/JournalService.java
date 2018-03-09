package com.groupdevotions.server.service;

import com.google.appengine.api.datastore.Key;
import com.google.code.twig.ObjectDatastore;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import com.groupdevotions.server.ServerUtils;
import com.groupdevotions.server.dao.JournalDAO;
import com.groupdevotions.shared.model.Account;
import com.groupdevotions.shared.model.Journal;

import java.util.*;
import java.util.logging.Logger;

public class JournalService {
    private Logger logger = Logger.getLogger(JournalService.class.getName());
    private final JournalDAO journalDAO;

    @Inject
    public JournalService(JournalDAO journalDAO) {
        this.journalDAO = journalDAO;
    }

    public String readInstructions(ObjectDatastore datastore, Account account) {
        String instructions = "This is your personal journal.  The contents are not visible to the group.";

        return instructions;
    }

    public Collection<Journal> read(ObjectDatastore datastore, Account account, Date lastDateAlreadyFetched) {
        ArrayList<Journal> journals = new ArrayList<Journal>();

        Date goalsAndRemindersDate = ServerUtils.fakeJournalDate();
        Date today = ServerUtils.removeTime(new Date());
        Date startAfterThisDate = lastDateAlreadyFetched;
        if (lastDateAlreadyFetched == null) {
            startAfterThisDate = ServerUtils.dateAddDays(goalsAndRemindersDate,1);
        }

        journals.addAll(journalDAO.readFirst(datastore, account.key, startAfterThisDate));
        addMissingStandardJournals(account, lastDateAlreadyFetched, journals, goalsAndRemindersDate, today);
        sortJournalsNewestToOldest(journals);
        return addNonStoredFields(journals);
    }

    private void sortJournalsNewestToOldest(ArrayList<Journal> journals) {
        Comparator<Journal> c = new Comparator<Journal>() {
            @Override
            public int compare(Journal arg0, Journal arg1) {
                return - arg0.forDay.compareTo(arg1.forDay);
            }
        };
        Collections.sort(journals, c);
    }

    private Collection<Journal> addNonStoredFields(ArrayList<Journal> journals) {
        return Collections2.transform(journals, new Function<Journal, Journal>() {
            @Override
            public Journal apply(Journal journal) {
                journal.forDateDisplay = ServerUtils.formatDateDisplayForTitle(journal.forDay);
                journal.htmlContent = ServerUtils.preserveWhitespace(journal.content);
                return journal;
            }
        });
    }

    private void addMissingStandardJournals(Account account, Date olderThan, ArrayList<Journal> journals, Date goalsAndRemindersDate, Date today) {
        if (olderThan == null) {
            if (!Journal.find(journals, goalsAndRemindersDate)) {
                addEmptyJournal(account, journals, goalsAndRemindersDate);
            }
            if (!Journal.find(journals, today)) {
                addEmptyJournal(account, journals, today);
            }
            Date yesterday = ServerUtils.dateAddDays(today, -1);
            if (!Journal.find(journals, yesterday)) {
                addEmptyJournal(account, journals, yesterday);
            }
        }
    }

    private void addEmptyJournal(Account account, ArrayList<Journal> journals, Date forDate) {
        Journal goalsAndRemindersJournal = new Journal();
        goalsAndRemindersJournal.forDay = forDate;
        goalsAndRemindersJournal.content = "";
        goalsAndRemindersJournal.accountKey = account.key;
        journals.add(goalsAndRemindersJournal);
    }

    public void save(ObjectDatastore datastore, Account account, Journal journal) {
        Journal originalJournal = journalDAO.readForDate(datastore, account.key, journal.forDay);
        if (originalJournal == null) {
            journal.accountKey = account.key;
            journalDAO.create(datastore, journal);
        } else {
            Key key = journalDAO.getKey(datastore, originalJournal);
            if (Strings.isNullOrEmpty(journal.content)) {
                journalDAO.delete(datastore, key);
            } else {
                originalJournal.content = journal.content;
                journalDAO.update(datastore, originalJournal, key);
            }
        }
    }
}
