import axios from "axios";

const client = axios.create();

const showFileChooser = () => {
  return new Promise((resolve, reject) => {
    client
      .get("/native/fileChooser")
      .then(response => resolve(response.data.data))
      .catch(error => reject(error));
  });
};

const showDirChooser = () => {
  return new Promise((resolve, reject) => {
    client
      .get("/native/dirChooser")
      .then(response => resolve(response.data.data))
      .catch(error => reject(error));
  });
};

export default {
  showFileChooser,
  showDirChooser
};
