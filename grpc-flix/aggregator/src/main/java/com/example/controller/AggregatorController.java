package com.example.controller;

import com.example.service.AggregatorService;
import com.example.vo.MovieVo;
import com.example.vo.UpdateGenreParam;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AggregatorController {

  @Autowired
  private AggregatorService aggregatorService;

  @GetMapping("/user/{loginId}/movies")
  public List<MovieVo> getUserMovies(@PathVariable String loginId) {
    return aggregatorService.getUserMovies(loginId);
  }

  @PutMapping("/user/updateGenre")
  public void updateUserGenre(@RequestBody UpdateGenreParam updateGenreParam) {
    aggregatorService.updateUserGenre(updateGenreParam);
  }
}
