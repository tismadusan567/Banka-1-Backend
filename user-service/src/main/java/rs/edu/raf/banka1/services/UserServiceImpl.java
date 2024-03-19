package rs.edu.raf.banka1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka1.dtos.PermissionDto;
import rs.edu.raf.banka1.mapper.PermissionMapper;
import rs.edu.raf.banka1.mapper.UserMapper;
import rs.edu.raf.banka1.model.Permission;
import rs.edu.raf.banka1.model.User;
import rs.edu.raf.banka1.repositories.PermissionRepository;
import rs.edu.raf.banka1.repositories.UserRepository;
import rs.edu.raf.banka1.requests.CreateUserRequest;
import rs.edu.raf.banka1.requests.EditUserRequest;
import rs.edu.raf.banka1.responses.ActivateAccountResponse;
import rs.edu.raf.banka1.responses.CreateUserResponse;
import rs.edu.raf.banka1.responses.EditUserResponse;
import rs.edu.raf.banka1.responses.UserResponse;
import rs.edu.raf.banka1.utils.JwtUtil;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Value("${front.port}")
    String frontPort;
    private UserMapper userMapper;
    private UserRepository userRepository;
    private PermissionRepository permissionRepository;

    private PermissionMapper permissionMapper;
    private EmailService emailService;
    private JwtUtil jwtUtil;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, EmailService emailService,
                           PermissionRepository permissionRepository,
                           JwtUtil jwtUtil, PermissionMapper permissionMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.permissionRepository = permissionRepository;
        this.jwtUtil = jwtUtil;
        this.permissionMapper = permissionMapper;
    }

    public UserResponse findByEmail(String email) {
        return this.userRepository.findByEmail(email).map(userMapper::userToUserResponse).orElse(null);
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(userMapper::userToUserResponse).toList();
    }

    @Override
    public UserResponse findById(Long id) {
        return userRepository.findById(id).map(userMapper::userToUserResponse).orElse(null);
    }

    @Override
    public UserResponse findByJwt() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if the user is authenticated
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            // Assuming your UserDetails implementation has the email field
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            return findByEmail(email);
        }

        return null;
    }

    @Override
    public List<UserResponse> search(String email, String firstName, String lastName, String position) {
        return userRepository.searchUsersByEmailAndFirstNameAndLastNameAndPosition(email, firstName, lastName, position)
                .map(users -> users.stream().map(userMapper::userToUserResponse).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public CreateUserResponse createUser(CreateUserRequest createUserRequest) {
        User user = userMapper.createUserRequestToUser(createUserRequest);
        String activationToken = UUID.randomUUID().toString();
        user.setActivationToken(activationToken);
        if (createUserRequest.getPosition().equalsIgnoreCase("admin")) {
            user.setPermissions(adminPermissions());
        }
        userRepository.save(user);
        emailService.sendActivationEmail(createUserRequest.getEmail(), "RAF Banka - User activation",
                "Visit this URL to activate your account: http://localhost:" + frontPort + "/user/set-password/" + activationToken);
        return new CreateUserResponse(user.getUserId());
    }

    //temporary
    private Set<Permission> adminPermissions() {
        Permission readPermission = new Permission();
        readPermission.setName("readUser");
        Permission addPermission = new Permission();
        addPermission.setName("addUser");
        Permission modifyPermission = new Permission();
        modifyPermission.setName("modifyUser");
        Permission deletePermission = new Permission();
        deletePermission.setName("deleteUser");

        permissionRepository.save(readPermission);
        permissionRepository.save(addPermission);
        permissionRepository.save(modifyPermission);
        permissionRepository.save(deletePermission);

        Set<Permission> adminPermissions = new HashSet<>();
        adminPermissions.add(readPermission);
        adminPermissions.add(addPermission);
        adminPermissions.add(modifyPermission);
        adminPermissions.add(deletePermission);

        return adminPermissions;
    }

    @Override
    public ActivateAccountResponse activateAccount(String token, String password) {
        User user = userRepository.findByActivationToken(token).orElseThrow();
        user.setActivationToken(null);
        user.setPassword(password);
        userRepository.save(user);
        return new ActivateAccountResponse(user.getUserId());
    }

    @Override
    public EditUserResponse editUser(EditUserRequest editUserRequest) {
        User user = userRepository.findByEmail(editUserRequest.getEmail()).orElseThrow();
        user = userMapper.editUserRequestToUser(user, editUserRequest);
        userRepository.save(user);
        return new EditUserResponse();
    }

    @Override
    public boolean deleteUser(Long id) {
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isPresent()) {
            User user = optUser.get();
            if (user.getActive()) {
                userRepository.deactivateUser(user.getUserId());
                return true;
            }
        }
        return false;
    }

    @Override
    public List<PermissionDto> findPermissions(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if(user == null)
            return null;
        return extractPermissionsFromUser(user);
    }

    @Override
    public List<PermissionDto> findPermissions(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null)
            return null;
        return extractPermissionsFromUser(user);
    }

    //necessary for authentication
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> myUser = this.userRepository.findByEmail(username);
        if (myUser.isEmpty()) {
            throw new UsernameNotFoundException("User name " + username + " not found");
        }

        User user = myUser.get();
        //convert permissions to list of simple granted authorities used by @PreAuthorize
        List<SimpleGrantedAuthority> authorities = user.getPermissions().stream()
                .map((permission -> new SimpleGrantedAuthority(permission.getName())))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }

    private List<PermissionDto> extractPermissionsFromUser(User user) {
        return user.getPermissions().stream().
                map(permissionMapper::permissionToPermissionDto).collect(Collectors.toList());
    }
}
