package com.example.demo.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Witchcraft
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBody {
    private int code;
    private Object data;
}
