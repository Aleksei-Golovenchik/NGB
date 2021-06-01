package com.epam.catgenome.entity.blast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlastDataBase {
    private Long id;
    private String name;
    private String path;
    private BlastDataBaseType type;
}
