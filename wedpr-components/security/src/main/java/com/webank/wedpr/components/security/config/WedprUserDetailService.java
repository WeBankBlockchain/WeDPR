package com.webank.wedpr.components.security.config;

import com.webank.wedpr.components.user.entity.WedprUser;
import com.webank.wedpr.components.user.entity.result.WedprUserRoleResult;
import com.webank.wedpr.components.user.service.WedprUserRoleService;
import com.webank.wedpr.components.user.service.WedprUserService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Created by caryliao on 2024/7/25 23:45 */
@Service
public class WedprUserDetailService implements UserDetailsService {

    @Autowired private WedprUserService wedprUserService;

    @Autowired private WedprUserRoleService wedprUserRoleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        WedprUser wedprUser = wedprUserService.getById(username);
        if (wedprUser != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            getAuthorities(username, authorities);
            return new User(username, wedprUser.getPassword(), authorities);
        } else {
            throw new UsernameNotFoundException("用户名不存在");
        }
    }

    private void getAuthorities(String username, List<GrantedAuthority> authorities) {
        List<WedprUserRoleResult> wedprUserRoleResultList =
                wedprUserRoleService.getWedprUserRoleByUsername(username);
        wedprUserRoleResultList.forEach(
                wedprUserRoleResult -> {
                    if (wedprUserRoleResult != null
                            && !StringUtils.isEmpty(wedprUserRoleResult.getRoleName())) {
                        GrantedAuthority grantedAuthority =
                                new SimpleGrantedAuthority(wedprUserRoleResult.getRoleName());
                        authorities.add(grantedAuthority);
                    }
                });
    }
}
