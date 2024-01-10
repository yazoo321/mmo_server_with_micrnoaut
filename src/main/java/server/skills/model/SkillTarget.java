package server.skills.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import server.common.dto.Location2D;

@Data
@AllArgsConstructor
public class SkillTarget {

    private String actorId; // either actorID or location has to be set
    private Location2D location;

    private int radius; // radius can be 0 for single target

}
