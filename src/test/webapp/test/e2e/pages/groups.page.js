var groupsPage = function () {
    var that = this;

    var title = by.tagName('h1');
    var editMembers= by.id('editMembers');
    var editGroup = by.id('editGroup');
    var addGroup = by.id('addGroup');
    var groupsRepeater = by.repeater('group in groups');

    return {
        title: title,
        editMembers: editMembers,
        editGroup: editGroup,
        addGroup: addGroup,
        groupsRepeater: groupsRepeater
    };
};

module.exports = groupsPage;
