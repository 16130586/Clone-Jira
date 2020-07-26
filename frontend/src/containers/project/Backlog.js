import React , {useEffect} from 'react'
import { connect } from 'react-redux'
import {pageContextualNavigation} from '../../actions/global'
const Backlog = function(props){
    useEffect(() => {
        props.getNavigation('BACKLOG', props.match.params)
    }, [])
    const {projectId} = props.match.params
    return(
        <div>specific project BACKLOG</div>
    )
}
const mapStateToProps = state => {
    return {
        
    }
}
const mapDispatchToProps = dispatch => {
    return {
        getNavigation : (pageName, data) => dispatch(pageContextualNavigation(pageName,data))
    }
}
export default connect(mapStateToProps, mapDispatchToProps)(Backlog)