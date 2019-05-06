package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DriveFolder {
    private final String name;
    private final String id;
    private final List<DriveFolder> children;

    public DriveFolder(String name, String id, List<DriveFolder> children) {
        this.name = name;
        this.id = id;
        this.children = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(children, "Children list must not be null")));
    }

    public List<DriveFolder> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
