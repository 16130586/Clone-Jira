import React, { useEffect } from 'react'
import { connect } from 'react-redux'

import BacklogComponent from '../../components/project/BacklogComponent'
import {navigateTo, pageContextualNavigation} from '../../actions/global'
import {
    fetchBacklogPage, requestDeleteSprint,
    requestTopOfBacklog, requestBottomOfBacklog,
    requestMoveUpSprint, requestMoveDownSprint,
    requestCreateSprint, requestEditSprint,
    requestStartSprint, requestDeleteIssue,
    requestMoveIssueToSprint, requestCreateNewIssue,
    requestCompleteSprint, requestUpdateDetailIssue,
}
    from '../../actions/project'
const Backlog = function (props) {
    const { projectId } = props.match.params
    useEffect(() => {
        props.getNavigation('BACKLOG', props.match.params)
    }, [])
    useEffect(() => {
        if (!props.isLoadBacklogPage) {
            props.fetchBacklogPage(projectId)
        }
    }, [props.isLoadBacklogPage])


    return (
        <BacklogComponent
            projectId={projectId}
            project={props.project}
            workflow={props.workflow}
            backlogItems={props.backlogItems}
            workingSprints={props.workingSprints}
            topOfBacklog={props.topOfBacklog}
            bottomOfBacklog={props.bottomOfBacklog}
            deleteSprint={props.deleteSprint}
            moveUpSprint={props.moveUpSprint}
            moveDownSprint={props.moveDownSprint}
            createSprint={props.createSprint}
            editSprint={props.editSprint}
            startSprint={props.startSprint}
            deleteIssue={props.deleteIssue}
            moveIssueToSprint={props.moveIssueToSprint}
            issueTypes={props.issueTypes}
            createNewIssue={props.createNewIssue}
            completeSprint={props.completeSprint}
            updateDetailIssue={props.updateDetailIssue}
            navigateTo={props.navigateTo}
        >
        </BacklogComponent>
    )
}
const mapStateToProps = state => {
    return {
        project : state.Project_Backlog.project,
        backlogItems: state.Project_Backlog.backlogItems,
        workingSprints: state.Project_Backlog.workingSprints,
        workflow: state.Project_Backlog.workflow,
        issueTypes : state.Project_Backlog.issueTypes,
        isLoadBacklogPage: state.Project_Backlog.isLoadBacklogPage,
    }
}
const mapDispatchToProps = dispatch => {
    return {
        getNavigation: (pageName, data) => dispatch(pageContextualNavigation(pageName, data)),
        fetchBacklogPage: (projectId) => dispatch(fetchBacklogPage(projectId)),
        topOfBacklog: (issueId, sprintId) => dispatch(requestTopOfBacklog(issueId, sprintId)),
        bottomOfBacklog: (issueId, sprintId) => dispatch(requestBottomOfBacklog(issueId, sprintId)),
        deleteSprint: (sprintId) => dispatch(requestDeleteSprint(sprintId)),
        moveUpSprint: (sprintId) => dispatch(requestMoveUpSprint(sprintId)),
        moveDownSprint: (sprintId) => dispatch(requestMoveDownSprint(sprintId)),
        createSprint: (projectId) => dispatch(requestCreateSprint(projectId)),
        editSprint: (data) => dispatch(requestEditSprint(data)),
        startSprint: (data) => dispatch(requestStartSprint(data)),
        deleteIssue: (issueId, projectId) => dispatch(requestDeleteIssue(issueId, projectId)),
        moveIssueToSprint: (fromSprintId, toSprintId, issueId) => dispatch(requestMoveIssueToSprint(fromSprintId, toSprintId, issueId)),
        createNewIssue: (data) => dispatch(requestCreateNewIssue(data)),
        completeSprint: (sprintId) => dispatch(requestCompleteSprint(sprintId)),
        updateDetailIssue : (data) => dispatch(requestUpdateDetailIssue(data)),
        navigateTo: (data) => dispatch(navigateTo(data))
    }
}
export default connect(mapStateToProps, mapDispatchToProps)(Backlog)