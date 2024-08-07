import { Scene } from "phaser";
import { createRandomAvatar, randomSkin } from "./Avatar";
import { controlAvatarAnimations } from "../Avatar/controlAvatar";

export class Map extends Scene {
  private avatar!: Phaser.GameObjects.Container;
  private keyboards!: Phaser.Types.Input.Keyboard.CursorKeys | null;

  constructor() {
    super("Map");
  }

  create() {
    const campingMap = this.make.tilemap({ key: "camping" });
    const campingGroundTilesets = campingMap.addTilesetImage(
      "camping_ground_tilesets",
      "camping_ground_tilesets"
    );

    if (campingGroundTilesets) {
      campingMap.createLayer("camping_ground", campingGroundTilesets);
    }

    this.avatar = createRandomAvatar(this, 550, 350);
    this.add.existing(this.avatar);
    this.cameras.main.setZoom(2);

    if (this.input.keyboard) {
      this.keyboards = this.input.keyboard.createCursorKeys();
    } else {
      this.keyboards = null;
    }
  }

  update() {
    if (this.keyboards && this.avatar) {
      controlAvatarAnimations(this.avatar, this.keyboards, randomSkin);
    }
  }
}
