package com.hello.vo.acl;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "管理员登录信息")
public class AdminLoginVo implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "管理员名称")
	private String username;

	@ApiModelProperty(value = "密码")
	private String password;

}