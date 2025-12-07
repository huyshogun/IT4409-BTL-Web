package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service  // Đánh dấu class này là Service
public class UserService {
    // Inject Repository vào
    @Autowired  // Dùng cách này cho ngắn, chứ thường là inject qua constructor
    private final UserRepository userRepository;

    public List<UserModel> getUserList() {
        // Lấy ra từ Repository dạng List<User> là Entity
        List<User> users = userRepository.findAll();

        // Biển đổi List<Entity> thành List<Model> (xem ở constructor của UserModel có code copy dữ liệu)
        List<UserModel> userModels = users.stream()
            .map(UserModel::new)
            .collect(Collector.toList());

        // Hoặc dùng cách bình thường (gà :D)
        // Chọn 1 trong 2 nhé
        List<UserModel> userModels = new ArrayList<>();
        for (User user: users)
            userModels.add(new UserModel(user));

        return userModels;  // Code khá nhiều so với Controller
    }
}
 {
    
}
