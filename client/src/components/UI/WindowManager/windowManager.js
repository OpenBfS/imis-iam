/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

const getDimensionsByEdge = (edge, windowHeight, windowWidth) => {
  let x = 0;
  let y = 0;
  let width = 0;
  let height = 0;
  if (edge === "top") {
    width = windowWidth;
    height = windowHeight;
  } else if (
    edge === "top-right" ||
    edge === "top-left" ||
    edge === "bottom-right" ||
    edge === "bottom-left"
  ) {
    width = windowWidth / 2;
    height = windowHeight / 2;
  } else if (edge === "right" || edge === "left") {
    width = windowWidth / 2;
    height = windowHeight;
  }

  if (edge.includes("right")) {
    x = windowWidth / 2;
  }
  if (edge.includes("bottom")) {
    y = windowHeight / 2;
  }

  return {
    height,
    width,
    x,
    y,
  };
};

export { getDimensionsByEdge };
