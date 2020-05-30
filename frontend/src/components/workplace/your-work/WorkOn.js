import React, { Fragment, useEffect } from 'react'
import LegenPaginationList from '../../customize/LegendPaginationList'
import { Link } from 'react-router-dom'
import moment from 'moment'
import { connect } from 'react-redux'
import {fetchWorkOnItem} from '../../../actions/work-space/index'
let ItemLayout = function (props) {
    return (
        <Fragment>
            <Link to={props.linkTo}
                className="work-on-item-container"
            >
                <div>
                    <img class="work-on-item_img" src="https://realng.atlassian.net/secure/viewavatar?size=medium&avatarId=10315&avatarType=issuetype"></img>
                </div>
                <span class="work-on-item_info">
                    <span>A40</span>
                    <small className="work-on-item_project">
                        <span>item name</span>
                        <span style={{ margin: "0px 8px" }}>·</span>
                        <span>Project name</span>
                    </small>
                </span>
                <span class="work-on-item_action">Created</span>
                <div className="work-on-item_author">
                    <div className="work-on-item_author_avatar-container">
                        <div className="work-on-item_author_avatar_img " style={{ backgroundImage: "url(https://image.shutterstock.com/image-photo/white-transparent-leaf-on-mirror-260nw-1029171697.jpg)" }}>
                        </div>
                    </div>
                </div>
            </Link>
        </Fragment>
    )
}

let getItemLegend = function (data) {
    let result = null
    if (data.lastTouch) {
        let currentDate = new Date()
        let current = moment("2020-05-13")
        let last = moment(data.lastTouch)
        let dateDiff = Math.abs(last.diff(current, 'days'))
        if (dateDiff < 1) {
            result = "TODAY"
        }
        else if (dateDiff > 1 && dateDiff <= 6) {
            result = "THIS MONTH"
        }
        else if (dateDiff >= 7 && dateDiff < 30) {
            result = "LAST MONTH"
        } else {
            result = "A LONG AGO"
        }
    }
    return result
}

let WorkOn = function (props) {
    useEffect(() => {
        if(props.isAvaiable){
            props.fetchMore(0 , props.pageSize)      
        }
    } , []); // it's seemed as componentDidMount
    return (
        <Fragment>
            <LegenPaginationList ItemComponent={ItemLayout}
                getItemLegend={getItemLegend}
                data={props.data}
                stillLoadMore={props.isAvaiable}
                loadMoreAction={() => props.fetchMore(props.page , props.pageSize)} />
        </Fragment>
    )
}

const mapStateToProps = state => {
    return {
      page: state.WorkSpace_WorkOn.page,
      pageSize : state.WorkSpace_WorkOn.pageSize,
      isAvaiable : state.WorkSpace_WorkOn.isAvaiable,
      data : state.WorkSpace_WorkOn.data
    }
  }
  
  const mapDispatchToProps = dispatch => {
    return {
        fetchMore : (page,pageSize) => dispatch(fetchWorkOnItem(page, pageSize))
    }
  }

export default connect(mapStateToProps, mapDispatchToProps)(WorkOn);