package com.github.rodrigo_sp17.mscheduler.push;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushEvent implements Serializable {
    private String type;
    private Map<String, Object> body;
}
