package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationMapper;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.sellergoods.service.SpecificationService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = SpecificationService.class)
public class SpecificationServiceImpl extends BaseServiceImpl<TbSpecification> implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public PageResult search(Integer page, Integer rows, TbSpecification specification) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbSpecification.class);
        Example.Criteria criteria = example.createCriteria();
        if(!StringUtils.isEmpty(specification.getSpecName())){
            criteria.andLike("specName", "%" + specification.getSpecName() + "%");
        }

        List<TbSpecification> list = specificationMapper.selectByExample(example);
        PageInfo<TbSpecification> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public void add(Specification specification) {
        //1、保存规格；通用Mapper在新增实体类之后会将自增的主键回填到该实体类对象中
        add(specification.getSpecification());

        //2、保存规格选项列表
        if (specification.getSpecificationOptionList() != null && specification.getSpecificationOptionList().size() > 0) {
            for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()) {

                tbSpecificationOption.setSpecId(specification.getSpecification().getId());
                //保存规格选项
                specificationOptionMapper.insertSelective(tbSpecificationOption);
            }
        }
    }

    @Override
    public Specification findOne(Long id) {
        Specification specification = new Specification();

        //规格
        specification.setSpecification(specificationMapper.selectByPrimaryKey(id));

        //根据规格id查询规格选项列表
        TbSpecificationOption param = new TbSpecificationOption();
        param.setSpecId(id);
        List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.select(param);
        specification.setSpecificationOptionList(specificationOptionList);

        return specification;
    }

    @Override
    public void update(Specification specification) {
        // 1、根据规格id更新规格
        update(specification.getSpecification());

        // 2、删除该规格对应的所有选项，
        //delete from tb_specification_option where spec_id = ?
        TbSpecificationOption param = new TbSpecificationOption();
        param.setSpecId(specification.getSpecification().getId());
        specificationOptionMapper.delete(param);

        // 3、再将前端传递的规格列表数据新增到数据库表中。
        if (specification.getSpecificationOptionList() != null && specification.getSpecificationOptionList().size() > 0) {
            for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()) {

                tbSpecificationOption.setSpecId(specification.getSpecification().getId());
                //保存规格选项
                specificationOptionMapper.insertSelective(tbSpecificationOption);
            }
        }
    }

    @Override
    public void deleteSpecificationByIds(Long[] ids) {
        //1、删除规格
        deleteByIds(ids);

        //2、删除规格对应的选项
        //delete from tb_specification_option  where spec_id in(?,?,...)
        Example example = new Example(TbSpecificationOption.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("specId", Arrays.asList(ids));

        specificationOptionMapper.deleteByExample(example);
    }

    @Override
    public List<Map<String, Object>> selectOptionList() {
        return specificationMapper.selectOptionList();
    }
}
