package com.example.demo.services.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.bean.Well;
import com.example.demo.mapper.WellMapper;
import com.example.demo.services.WellService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author VOF
 * @since 2023-04-16
 */
@Service
public class WellServiceImpl extends ServiceImpl<WellMapper, Well> implements WellService {
    /**
     * 删除所有 id
     * @param ids
     */
    @Override
    public void deleteSelected(String ids) {
        if (ids.contains(",")) {
            String[] split = ids.split(",");
            List<Integer> list = new ArrayList<Integer>();
            for (String s : split) {
                int i = 0;
                try {
                    i = Integer.parseInt(s);
                } catch (NumberFormatException e) {

                }
                list.add(i);
            }
            for (int id : list) {
                this.removeById(id);
            }
        }else {
            this.removeById(Integer.parseInt(ids));
        }
    }
}
