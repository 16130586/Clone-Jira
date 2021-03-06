package nlu.project.backend.DAO;

import lombok.Data;
import lombok.NoArgsConstructor;
import nlu.project.backend.business.FileBusiness;
import nlu.project.backend.entry.filter.ProjectFilterParams;
import nlu.project.backend.entry.project.ProjectParams;
import nlu.project.backend.entry.project.UserRoleParams;
import nlu.project.backend.entry.project.WorkFlowParams;
import nlu.project.backend.exception.custom.InternalException;
import nlu.project.backend.model.*;
import nlu.project.backend.repository.*;
import nlu.project.backend.util.constraint.ConstraintRole;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
@Data
public class ProjectDAO {
    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    BacklogRepository backlogRepository;


    @Autowired
    WorkflowRepository workflowRepository;

    @Autowired
    WorkFlowItemRepository itemRepository;

    @Autowired
    IssueTypeRepository issueTypeRepository;

    @Autowired
    SprintVelocityDAO sprintVelocityDAO;

    @Autowired
    FileBusiness fileBusiness;

    public boolean isExistedProjectName(String name) {
        return projectRepository.existsByName(name);
    }

    public boolean isExistedProjectCode(String code) {
        return projectRepository.existsByCode(code);
    }

    public Project getProjectById(int id) {
        return projectRepository.getOne(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = IllegalArgumentException.class)
    public Project save(ProjectParams projectParams) {
        String imgUrl = "";
        if (projectParams.file != null)
            imgUrl = fileBusiness.save(projectParams.file);
        projectParams.setImgUrl(imgUrl);
        // Project
        Project project = new Project();
        project.setName(projectParams.name);
        project.setCode(projectParams.key);
        project.setDescription(projectParams.description);
        project.setImgUrl(projectParams.imgUrl);
        project.setCurrentWorkFlow(workflowRepository.findDefault().get(0));
        projectRepository.save(project);
        // Backlog
        BackLog backLog = new BackLog();
        backLog.setProject(project);
        backlogRepository.save(backLog);
        project.setBacklog(backLog);

        if (projectParams.leader != projectParams.productOwner) {
            // Leader
            User teamLead = userRepository.getOne(projectParams.leader);
            Role leadRole = roleRepository.findByName(ConstraintRole.TEAM_LEAD);
            UserRole leader = new UserRole();
            leader.setUser(teamLead);
            leader.setRole(leadRole);
            leader.setProject(project);
            userRoleRepository.save(leader);
            // Product Owner
            User creator = userRepository.findById(projectParams.productOwner).get();
            Role poRole = roleRepository.findByName(ConstraintRole.PRODUCT_OWNER);
            UserRole productOwner = new UserRole();
            productOwner.setUser(creator);
            productOwner.setRole(poRole);
            productOwner.setProject(project);
            userRoleRepository.save(productOwner);


            project.setOwner(creator);
            project.setLeader(teamLead);

            if(teamLead.getJointProjects() != null)
                teamLead.getJointProjects().add(project);
            projectRepository.saveAndFlush(project);


        } else {
            User creator = userRepository.findById(projectParams.productOwner).get();
            Role poRole = roleRepository.findByName(ConstraintRole.PRODUCT_OWNER);
            Role leadRole = roleRepository.findByName(ConstraintRole.TEAM_LEAD);
            UserRole productOwner = new UserRole();
            productOwner.setUser(creator);
            productOwner.setRole(poRole);
            productOwner.setProject(project);
            userRoleRepository.save(productOwner);
            UserRole leader = new UserRole();
            leader.setUser(creator);
            leader.setRole(leadRole);
            leader.setProject(project);
            userRoleRepository.save(leader);
            project.setOwner(creator);
            project.setLeader(creator);

            if(creator.getJointProjects() != null)
                creator.getJointProjects().add(project);
            projectRepository.saveAndFlush(project);

        }
        // End
        return project;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = IllegalArgumentException.class)
    public Project update(Project project, ProjectParams projectParams) {
        System.out.println("here");
        Role leadRole = roleRepository.findByName(ConstraintRole.TEAM_LEAD);
        UserRole leader = userRoleRepository.findByRoleAndProject(leadRole, project);
        if (leader.getUser().getId() != projectParams.leader) {
            User newTeamLead = userRepository.getOne(projectParams.leader);
            leader.setUser(newTeamLead);
            userRoleRepository.save(leader);
        }
        if (projectParams.workFlow != null) {
            WorkFlow workFlow = workflowRepository.getOne(projectParams.workFlow);
            project.setCurrentWorkFlow(workFlow);
        }
        project.setName(projectParams.name);
        project.setCode(projectParams.key);
        project.setDescription(projectParams.description);
        projectRepository.save(project);
        return project;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = IllegalArgumentException.class)
    public boolean delete(int projectId) {
        try {
            Project project = projectRepository.getOne(projectId);
            project.setDeleted(true);
            projectRepository.saveAndFlush(project);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public List<Project> findByNameLike(String name) {
        List<Project> result = projectRepository
                .findByNameContaining(name)
                .stream().filter(p -> !p.isDeleted())
                .collect(Collectors.toList());
        return result;
    }

    public List<Project> findByCode(String key) {
        List<Project> result = projectRepository
                .findByCode(key)
                .stream().filter(p -> !p.isDeleted())
                .collect(Collectors.toList());
        return result;
    }

    public List<Project> findByKeyLike(String key) {
        List<Project> result = projectRepository
                .findByCodeContaining(key)
                .stream().filter(p -> !p.isDeleted())
                .collect(Collectors.toList());
        return result;
    }

    public List<Project> findByUser(int userId) {
        User user = userRepository.getOne(userId);
        final List<Project> result = new ArrayList<>();
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        userRoles.forEach(userRole -> result.add(userRole.getProject()));
        return result.stream().filter(p -> !p.isDeleted()).collect(Collectors.toList());
    }

    public List<Project> findByOwner(int ownerId) {
        User user = userRepository.getOne(ownerId);
        List<Project> result = new ArrayList<>();
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        for (UserRole i : userRoles) {
            if (i.getRole().getId() == 1)
                result.add(i.getProject());
        }
        return result.stream().filter(p -> !p.isDeleted()).collect(Collectors.toList());
    }

    public List<Project> findByFilter(ProjectFilterParams filter) {
        List<Project> result;
        String name = (filter.name == null) ? "" : filter.name;
        String key = (filter.key == null) ? "" : filter.key;
        if ((filter.name != null) || (filter.key != null)) {
            result = projectRepository.findByNameContainingAndCodeContaining(name, key);
            result = filterByUserId(result, filter);
            result = filterByOwnerId(result, filter);
            return result;
        }
        if (filter.userId != null) {
            result = findByUser(filter.userId);
            result = filterByOwnerId(result, filter);
            return result;
        }
        if (filter.ownerId != null)
            return findByOwner(filter.ownerId);
        return findByNameLike("");
    }

    public List<Project> filterByOwnerId(List<Project> projectList, ProjectFilterParams filter) {
        if (filter.ownerId == null)
            return projectList;

        List<Project> result = new ArrayList<>();
        User owner = userRepository.getOne(filter.ownerId);
        UserRole userRole;
        for (Project project : projectList) {
            userRole = userRoleRepository.findByUserAndProject(owner, project);
            if (userRole != null && userRole.getRole().getId() == 1)
                result.add(project);
        }
        return result.stream().filter(pro -> !pro.isDeleted()).collect(Collectors.toList());
    }

    public List<Project> filterByUserId(List<Project> projectList, ProjectFilterParams filter) {
        if (filter.userId == null)
            return projectList;

        List<Project> result = new ArrayList<>();
        User owner = userRepository.getOne(filter.ownerId);
        UserRole userRole;
        for (Project project : projectList) {
            userRole = userRoleRepository.findByUserAndProject(owner, project);
            if (userRole != null)
                result.add(project);
        }
        return result;
    }

    public List<Project> findJointIn(int userId) {
        return projectRepository.findJointIn(userId);
    }

    public WorkFlow createWorkFlow(WorkFlowParams params) {
        Project project = getProjectById(params.projectId);
        WorkFlow workFlow = new WorkFlow();
        workFlow.setName(params.name);
        workFlow.setProject(project);
        return workflowRepository.save(workFlow);
    }

    public WorkFlowItem addWorkFlowItem(WorkFlowParams params) {
        WorkFlow workFlow = workflowRepository.getOne(params.id);
        WorkFlowItem item = new WorkFlowItem();
        item.setName(params.itemName);
        if (params.isStart)
            item.setColor("green");
        else if (params.isEnd)
            item.setColor("blue");
        else
            item.setColor("lightgreen");
        item.setStart(params.isStart);
        item.setEnd(params.isEnd);
        item.setLocation("0 0");
        item.setWorkFlow(workFlow);
        return itemRepository.save(item);
    }

    public WorkFlowItem addLinkWorkFlow(WorkFlowParams params) {
        WorkFlowItem item = itemRepository.getOne(params.fromItemId);
        WorkFlowItem toItem = itemRepository.getOne(params.toItemId);
        item.getNextItems().add(toItem);
        return itemRepository.save(item);
    }

    public WorkFlowItem deleteLinkWorkFlow(WorkFlowParams params) {
        WorkFlowItem item = itemRepository.getOne(params.fromItemId);
        WorkFlowItem toItem = itemRepository.getOne(params.toItemId);
        item.getNextItems().remove(toItem);
        return itemRepository.save(item);
    }

    public WorkFlowItem deleteWorkFlowItem(WorkFlowParams params) {
        WorkFlowItem item = itemRepository.getOne(params.toItemId);
        item.setWorkFlow(null);
        return itemRepository.save(item);
    }

    public WorkFlow updateWorkFlow(WorkFlow workFlow) {
        return workflowRepository.save(workFlow);
    }

    public WorkFlow getWorkFlowById(int id) {
        return workflowRepository.getOne(id);
    }

    public List<IssueType> getIssueTypes(Integer projectId) {
        try {
            List<IssueType> result = issueTypeRepository.findAllByProjectId(projectId);
            if (result.size() <= 0) {
                return issueTypeRepository.findDefaultIssueTypes();
            }
            return Collections.emptyList();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new InternalException(e.getMessage());
        }
    }

    public List<WorkFlow> getWorkFlowByProjectId(int projectId) {
        Project project = projectRepository.getOne(projectId);
        return workflowRepository.findByProjectOrProjectIsNullOrderByProjectAsc(project);
    }

    public UserRole addMember(UserRoleParams params) {
        User user = userRepository.getOne(params.userID);
        Role defaultRole = roleRepository.getOne(3);
        Project project = projectRepository.getOne(params.projectID);

        UserRole userRole = userRoleRepository.findByUserAndProject(user, project);
        // check if user in the project
        if (userRole != null) {
            return null;
        }
        userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(defaultRole);
        userRole.setProject(project);
        return userRoleRepository.save(userRole);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW , rollbackFor = IllegalArgumentException.class)
    public Project removeMember(UserRoleParams params) {
        User user = userRepository.getOne(params.userID);
        Project project = projectRepository.getOne(params.projectID);
        UserRole userRole = userRoleRepository.findByUserAndProject(user, project);


        Collection<Project> projects = user.getJointProjects();
        projects.remove(project);
        user.setJointProjects(projects);
        userRepository.save(user);
        userRoleRepository.delete(userRole);

        Collection<User> devTeam = project.getDevTeam();
        devTeam.remove(user);
        project.setDevTeam(devTeam);

        return project;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW , rollbackFor = IllegalArgumentException.class)
    public Project addMemberByUserName(UserRoleParams params) {
        User user = userRepository.findByUserName(params.userName);
        Project project = projectRepository.getOne(params.projectID);
        Role defaultRole = roleRepository.getOne(3);

        UserRole userRole = userRoleRepository.findByUserAndProject(user, project);
        // check if user in the project
        if (userRole != null) {
            return project;
        }
        userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(defaultRole);
        userRole.setProject(project);
        userRoleRepository.save(userRole);

        Collection<Project> projects = user.getJointProjects();
        projects.add(project);
        user.setJointProjects(projects);
        userRepository.save(user);

        Collection<User> devTeam = project.getDevTeam();
        devTeam.add(user);
        project.setDevTeam(devTeam);

        return project;
    }

    public UserRole addRoleToMember(UserRoleParams params) {
        User user = userRepository.getOne(params.userID);
        Role role = roleRepository.getOne(params.roleID);
        Project project = projectRepository.getOne(params.projectID);
        UserRole userRole = userRoleRepository.findByUserAndProject(user, project);
        userRole.setRole(role);
        return userRoleRepository.save(userRole);
    }

    public UserRole removeRoleFromMember(UserRoleParams params) {
        User user = userRepository.getOne(params.userID);
        Role role = roleRepository.getOne(params.roleID);
        Project project = projectRepository.getOne(params.projectID);
        UserRole userRole = userRoleRepository.findByUserAndProject(user, project);
        userRole.setRole(null);
        return userRoleRepository.save(userRole);
    }

    public Integer deleteWorkFlow(WorkFlowParams params) {
        workflowRepository.deleteById(params.id);
        return params.id;
    }

    public List<SprintVelocity> getVelocities(Integer projectId) {
        return sprintVelocityDAO.getVelocities(projectId);
    }
}
