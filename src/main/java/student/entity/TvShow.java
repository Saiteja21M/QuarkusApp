package student.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.net.URL;
import java.util.Set;

@Entity
public class TvShow extends PanacheEntityBase {

    @Id
    private int tvShowId;
    private URL url;
    private String name;
    private Set<String> genres;

    public int getTvShowId() {
        return tvShowId;
    }

    public void setTvShowId(int tvShowId) {
        this.tvShowId = tvShowId;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getGenres() {
        return genres;
    }

    public void setGenres(Set<String> genres) {
        this.genres = genres;
    }

    @Override
    public String toString() {
        return "TvShow{" +
                "tvShowId=" + tvShowId +
                ", url=" + url +
                ", name='" + name + '\'' +
                ", genres=" + genres +
                '}';
    }
}
