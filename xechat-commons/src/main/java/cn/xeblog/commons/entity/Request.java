package cn.xeblog.commons.entity;

import cn.xeblog.commons.enums.Action;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author anlingyi
 * @date 2020/5/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private T body;

    private Action action;

}
