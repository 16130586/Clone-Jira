import { ASYNC as AsyncEventTypes } from '../../constants/index'
const init = {
    priority:[],
    devTeam:[],
    issueTypes:[
        {id:'1',name:'Task'},
        {id:'2',name:'Bug'}
    ],
    issue: null,
    workFlow: null,
    project: null,
    subTasks:[],
    comments: []
}

const IssueReducer = (state = init, action) => {
    let nextState = state;
    switch(action.type) {
        case AsyncEventTypes.FULL_FILLED.FETCH_ISSUE:
            nextState = {
                issue: action.payload.issue,
                devTeam: action.payload.devTeam,
                priority: action.payload.priority,
                project: action.payload.project,
                workFlow: action.payload.workFlow,
                subTasks: action.payload.subTasks,
                issueTypes:[
                    {id:'1',name:'Task'},
                    {id:'2',name:'Bug'}
                ],
                comments: action.payload.comments
            }
            break;
        case AsyncEventTypes.FULL_FILLED.CREATE_SUBTASK:
            nextState = {...nextState, subTasks: [...nextState.subTasks, action.payload]}
            break;
        case AsyncEventTypes.FULL_FILLED.UPDATE_ISSUE_DESCRIPTION:
            nextState = {...nextState, issue: action.payload}
            break;
        case AsyncEventTypes.FULL_FILLED.COMMENT_ISSUE:
            nextState = {...nextState, comments: [...nextState.comments, action.payload]}
            break;
        default:
            break;
    }
    console.log(nextState)
    return nextState;
}

export default IssueReducer