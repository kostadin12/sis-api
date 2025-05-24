package com.kostadin.sis.user.color;

import com.kostadin.sis.config.UserProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserColorService {

    private final UserProperties userProperties;

    private final Random random = new Random();

    public String generateColor(List<String> allColors) {
        while (true){
            var color = generateNewColor();
            log.debug("Generated new color {}.",color);
            if (!foundSimilarColors(color, allColors, userProperties.getColor().getTolerance())){
                return color;
            }
        }
    }

    public List<String> generateColorPalette(int count, List<String> allUserColors) {
        var colors = new ArrayList<String>();

        while (colors.size() < count) {
            colors.add(generateColor(allUserColors));
        }
        return colors;
    }

    private String generateNewColor() {
        StringBuilder hex = new StringBuilder("#");
        for (int i = 0; i < 6; i++) {
            hex.append(
                    Integer.toHexString(random.nextInt(16))
            );
        }
        return hex.toString();
    }

    private boolean foundSimilarColors(String color, List<String> allColors, int tolerance){
        for (String c : allColors){
            if (areColorsSimilar(color, c, tolerance)){
                return true;
            }
        }

        for (String ignoredColor : userProperties.getColor().getIgnoredColors()){
            if (areColorsSimilar(color, ignoredColor, tolerance)){
                return true;
            }
        }
        return false;
    }

    private static boolean areColorsSimilar(String hexColor1, String hexColor2, int tolerance){
        int rgb1 = Integer.parseInt(hexColor1.substring(1), 16);
        int rgb2 = Integer.parseInt(hexColor2.substring(1), 16);

        int red1 = (rgb1 >> 16) & 0xFF;
        int green1 = (rgb1 >> 8) & 0xFF;
        int blue1 = rgb1 & 0xFF;

        int red2 = (rgb2 >> 16) & 0xFF;
        int green2 = (rgb2 >> 8) & 0xFF;
        int blue2 = rgb2 & 0xFF;

        double distance = Math.sqrt((double) (red1 - red2)*(red1 - red2) + (green1 - green2)*(green1 - green2) + (blue1 - blue2)*(blue1 - blue2));

        return distance <= tolerance;
    }
}
