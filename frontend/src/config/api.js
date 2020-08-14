export const BACKEND_API = {
    BASE_URL: "http://localhost:8081",
    ACTIONS: {
        LOGIN: "/user/login",
        SIGN_UP: "/user/registry",
        VALIDATE_TOKEN: "/user/validateToken",
        CREATE_PROJECT: "/project/create",
        PROJECT_GET_ONE : "/project/{projectId}",
        PROJECT_GET_ALL: "/project/jointIn",
        DELETE_PROJECT : "/project/delete",
        SEARCH_PROJECT: "/project/searchByFilter",
        WORKFLOW_CHART: "/chart/workflow",
        CREATE_WORKFLOW: "/project/workflow/create",
        ADD_WORKFLOW_ITEM: "/project/workflow/add-item",
        ADD_WORKFLOW_LINK : "/project/workflow/add-link",
        PROJECT_GET_BACKLOG_ITEMS : "/project/{projectId}/backlog",
        PROJECT_GET_WORKING_SPRINTS : "/project/{projectId}/workingSprints",
        GET_WORKFLOW : "/project/{projectId}/workflow",
        DELETE_SPRINT : "/sprint/{id}",
        TOP_OF_BACKLOG : "/issue/{id}/topBacklog",
        BOTTOM_OF_BACKLOG : "/issue/{id}/bottomBacklog",
        MOVE_UP_SPRINT : "/sprint/{id}/moveUp",
        MOVE_DOWN_SPRINT: "/sprint/{id}/moveDown",
        CREATE_SPRINT: "/sprint",
        EDIT_SPRINT : "/sprint/{id}",
        START_SPRINT : "/sprint/{id}/start",
        DELETE_ISSUE : "/issue/delete",
        MOVE_ISSUE: "/issue/move",
        PROJECT_GET_ISSUE_TYPES: '/project/{projectId}/issuetypes',
        CREATE_NEW_ISSUE : '/issue/create',
        COMPLETE_SPRINT: '/sprint/{id}/end',
        ISSUE_UPDATE_DETAIL : '/issue/updateDetail',
        DEV_TEAM : '/project/{projectId}/devteam',
        DELETE_WORKFLOW: '/project/workflow/delete',
        MY_GROUP : '/group',
        ABOUT_ME: '/user/me',
        GROUP_ADD_MEMBER: '/group/add',
        GROUP_REMOVE_MEMBER: '/group/remove',
        CREATE_GROUP : '/group/create',
        DELETE_GROUP : '/group/delete',
        CURRENT_SPRINT: '/project/{projectId}/currentSprint',
        DAD_ISSUE : '/issue/dad',
        FETCH_ISSUE: '/issue/{issueId}',
        FETCH_PRIORITY : '/issue/priority',
        CREATE_SUBTASK: '/issue/subTask/create',
        FETCH_SUBTASK: '/issue/subTask/getByIssue',
        FILTER_ISSUE_IN_SPRINT: '/sprint/searchIssueInSprintIssue',
    }
}