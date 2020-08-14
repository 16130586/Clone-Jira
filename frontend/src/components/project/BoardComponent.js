import React, { Fragment, useState, useEffect, createRef, useRef } from 'react'
import Typography from '@material-ui/core/Typography'
import Breadcrumbs from '@material-ui/core/Breadcrumbs'
import Link from '@material-ui/core/Link'
import { withStyles } from "@material-ui/core/styles"
import MuiAccordion from "@material-ui/core/Accordion"
import MuiAccordionSummary from "@material-ui/core/AccordionSummary"
import MuiAccordionDetails from "@material-ui/core/AccordionDetails"
import ExpandMoreIcon from "@material-ui/icons/ExpandMore"
import Button from '@material-ui/core/Button'
import { useDrag, useDrop } from 'react-dnd'

let IssueBox = function (props) {
    let description = props.data.description
    let issueType = props.data.issueType
    let issueName = props.data.name
    let issueId = props.data.id
    let storyPoint = props.data.storyPoint
    let assignment = props.data.assignment
    let assignee = (assignment && assignment.userName) ? assignment.userName : 'unassigned'
    const [{ opacity }, dragRef] = useDrag({
        item: {
            type: props.data.status.name,
            id: issueId,
            fromAssignee: assignee,
            fromStatusId: props.data.status.id,
        },
        collect: (monitor) => ({
            opacity: monitor.isDragging() ? 0.4 : 1,
        }),
    })

    let storyPointStyle = {
        borderRadius: "2rem",
        display: "inline-block",
        fontSize: ".86rem",
        padding: "0.166667em 0.5em",
        height: "1.2rem",
    };
    if (props.data.status.isStart) {
        storyPointStyle = { ...storyPointStyle, backgroundColor: "rgb(0, 82, 204)", color: "rgb(255, 255, 255)" }
    }
    else if (props.data.status.isEnd) {
        storyPointStyle = { ...storyPointStyle, backgroundColor: "rgb(0, 135, 90)", color: "rgb(255, 255, 255)" }
    } else {
        storyPointStyle = { ...storyPointStyle, backgroundColor: "rgb(223, 225, 230)", color: "rgb(23, 43, 77)" }
    }

    return (
        <div
            ref={dragRef}
            className={"my-btn bg--white my-btn--hover ".concat(props.className || '')}
            style={{
                width: "90%",
                height: "auto",
                padding: "1rem"
            }}>
            <div>
                {description}
            </div>
            <div className="mt-1"
                style={{
                    display: "flex",
                    alignItems: "center"
                }}>
                <div
                    style={{ display: "flex", alignItems: "center" }}>
                    <img
                        style={{ width: "1rem", height: "1rem" }}
                        src={issueType.iconUrl}
                        alt='' />
                    <Typography
                        style={{ fontSize: "1rem" }}
                        className="ml-1">{issueName}</Typography>
                </div>
                <div style={{ marginLeft: "auto", display: "flex", alignItems: "center" }}>
                    <span style={storyPointStyle}>{storyPoint}</span>
                    {assignment && assignment.avatarUrl &&
                        <img
                            style={{
                                width: "2.5rem",
                                height: "2.5rem",
                                borderRadius: "50%"
                            }}
                            className="ml-1"
                            alt=''
                            src={assignment.avatarUrl} />
                    }

                </div>
            </div>
        </div>
    )
}
let MyBreadCrumbs = function (props) {
    console.log(props)
    let projectId = props.project ? props.project.id : 0
    let projectCode = props.project ? props.project.code : ''
    let projectName = props.project ? props.project.name : ''
    return (
        <Breadcrumbs aria-label="breadcrumb" style={{ marginTop: "1.5rem" }}>
            <Link color="inherit" href="/projects">
                Projects
        </Link>
            <Link color="inherit" href={`/project/${projectId}/settings/details`}>
                {projectCode == null ? projectName : projectCode}
            </Link>
            <Typography color="textPrimary">Board</Typography>
        </Breadcrumbs>
    )
}
let BoardColumn = function (props) {

    let onDrop = function (data) {
        let submitData = {
            ...data,
            toStatusId: props.current.id,
            toAssignee: props.owner
        }
        if (submitData.fromAssignee == submitData.toAssignee && submitData.fromStatusId == submitData.toStatusId)
            return
        if (props.onDropIssueBox) props.onDropIssueBox(submitData)
    }
    const [{ isOver, canDrop }, dropRef] = useDrop({
        accept: props.all.map(item => item.name),
        drop: (item) => onDrop(item),
        collect: (monitor) => ({
            isOver: monitor.isOver(),
            canDrop: monitor.canDrop()
        })
    })
    return (
        <div
            ref={dropRef}
            style={{ ...props.style, witdth: "auto", minHeight: "8rem" }}>
            <Typography>{props.current.name} - {props.issues && props.issues.length}</Typography>
            {props.issues && props.issues.map(iss => <IssueBox className="mt-1" data={iss} />)}
        </div>
    )
}


const Accordion = withStyles({
    root: {
        border: "none",
        boxShadow: "none",
        "&:before": {
            display: "none"
        },
        "&:after": {
            display: "none"
        },
        "&$expanded": {
            margin: "auto"
        }
    },
    expanded: {}
})(MuiAccordion);

const AccordionSummary = withStyles({
    root: {
        backgroundColor: "white",
        marginBottom: -1,
        marginLeft: "-2rem",
        minHeight: 56,
        "&$expanded": {
            minHeight: 56
        }
    },
    content: {
        "&$expanded": {
            margin: "12px 0"
        }
    },
    expanded: {}
})(MuiAccordionSummary);

const AccordionDetails = withStyles(theme => ({
    root: {
        padding: "0px",
        paddingTop: "0px"
    }
}))(MuiAccordionDetails);

let UserIssueWorkSpace = function (props) {
    let [isExpanding, setIsExpanding] = useState(false)


    return (
        <Fragment>
            <Accordion
                className={props.className}
                square
                expanded={isExpanding}
                onChange={() => {
                    setIsExpanding(!isExpanding)
                }}

            >
                <AccordionSummary
                    className="Mui-accordion-summary--reverse"
                    expandIcon={<ExpandMoreIcon className="flex-order-1" />}
                >

                    <div style={{ display: "flex", width: "100%" }}>
                        <div style={{ flexGrow: 2, display: "flex", alignItems: "center" }}>
                            <Typography>
                                <span className="text--bold" style={{ textTransform: "uppercase" }}>
                                    {props.assignee}
                                </span>
                            </Typography>
                            <span className="ml-1"
                                style={{
                                    color: "rgb(107, 119, 140)",
                                    marginLeft: ".7rem",
                                    fontSize: "1rem"
                                }}
                            >
                                {props.issues && (props.issues.length > 1 ? props.issues.length + " issues" : props.issues.length + " issue")}
                            </span>
                        </div>
                        <div style={{ flexGrow: 8, display: "flex", flexDirection: "row-reverse" }}>

                        </div>
                    </div>
                </AccordionSummary>
                <AccordionDetails>
                    <div style={{ width: "100%", display: "flex" }}>
                        {
                            props.workflow && props.workflow.items && props.workflow.items.map((workflowItem, index) =>
                                <BoardColumn
                                    style={{
                                        display: "inline-block",
                                        padding: "1rem",
                                        marginLeft: index > 0 ? "2rem" : "0",
                                        width: "16rem",
                                        height: "auto",
                                        borderRadius: "5px",
                                        background: "rgb(244, 245, 247)"
                                    }}
                                    issues={props.issues.filter(iss => iss.status.id == workflowItem.id)}
                                    current={workflowItem}
                                    all={props.workflow.items}
                                    owner={props.assignee}
                                    onDropIssueBox={props.onDropIssueBox}
                                />
                            )
                        }
                    </div>
                </AccordionDetails>
            </Accordion>
        </Fragment>
    )
}
let BoardSpaceComponent = function (props) {
    let issueArgtor = {
        unassigned: []
    }
    if (props.devTeam) {
        props.devTeam.forEach(dev => issueArgtor[dev.userName] = [])
    }
    if (props.sprint && props.sprint.issues.length > 0) {
        props.sprint.issues.forEach(iss => {
            if (iss.assignment != undefined && iss.assignment != null) {
                if (issueArgtor[iss.assignment.userName] == null) {
                    issueArgtor[iss.assignment.userName] = []
                }
                issueArgtor[iss.assignment.userName].push(iss)
            } else
                issueArgtor['unassigned'].push(iss)
        })
    }

    return (
        <div className="pd-2">
            <MyBreadCrumbs project={props.project} />
            {
                props.workflow && props.workflow.items &&
                <div className="mt-3">
                    {props.workflow.items.map((workflowItem, index) =>
                        <div
                            style={{
                                display: "inline-block",
                                padding: "1rem",
                                marginLeft: index > 0 ? "2rem" : "0",
                                width: "16rem",
                                height: "auto",
                                borderRadius: "5px",
                                background: "rgb(244, 245, 247)"
                            }}
                        >
                            <Typography>{workflowItem.name}</Typography>
                        </div>
                    )
                    }
                </div>

            }
            {props.sprint != null && props.sprint.issues.length > 0 && Object.keys(issueArgtor).map(assignee =>
                <UserIssueWorkSpace
                    assignee={assignee}
                    issues={issueArgtor[assignee]}
                    workflow={props.workflow}
                    onDropIssueBox={props.onDropIssueBox}
                />)
            }
            {
                (props.sprint == null || props.sprint.issues.length == 0) &&
                <div className="mt-5" style={{ width: "100%", textAlign: "center" }}>
                    <Typography>Board will ready while you're starting sprint and working with sprint backlog </Typography>
                    <Button
                        color="primary"
                        variant="contained"
                        className="mt-2"
                        onClick={() => props.navigateTo(`/project/${props.projectId}/backlog`)}
                    >
                        Continue
                    </Button>
                </div>
            }
        </div>
    )
}

export default BoardSpaceComponent