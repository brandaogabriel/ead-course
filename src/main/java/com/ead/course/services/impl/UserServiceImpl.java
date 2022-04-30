package com.ead.course.services.impl;

import com.ead.course.repositories.UserRepository;
import com.ead.course.services.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository courseUserRepository;

    public UserServiceImpl(UserRepository courseUserRepository) {
        this.courseUserRepository = courseUserRepository;
    }
}
