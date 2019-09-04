package com.ccc.oa.service;

import com.ccc.oa.model.Department;
import com.ccc.oa.model.Member;

import java.util.List;

public interface DepartmentService {
    int deleteById(Long id);

    int insert(Department department);

    List<Department> selectAllDepartment();

    List<Department> selectChildren(Long id);

    Department selectById(Long id);

    List<Member> selectUsers(Long id);

    int updateById(Department department);
}
